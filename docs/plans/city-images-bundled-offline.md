# 城市图片内置化:彻底摆脱网络图源不稳定

## Summary

四轮网络图源修复均因用户网络环境失败。根因已实锤:

1. **开 VPN 全挂**:Wikipedia 按 open-proxy 政策封禁 VPN 出口 IP;Openverse(Cloudflare)可能拦截 OkHttp 指纹;cn.bing.com 海外出口页面结构不同 → 三级源全失败。
2. **不满 Hero**:不挂 VPN 时走 Bing,其 `c=7` 缩略图是 **letterbox JPEG(照片居中+四周纯白边)**(已实测像素+看图确认),paint/Crop 铺满后白边占据 Hero 四周。

**方案:放弃运行时搜图,把 103 个城市的代表图内置进 APK assets。** 开发机批量下载(Wikipedia 主源/Openverse 兜底)→ 居中裁剪 800×450 → JPEG 压缩。运行时本地加载,开不开 VPN 都有图、秒显、Hero 必铺满、内容人工可控,且契合计划书"离线优先"理念。

## Current State Analysis

- 城市清单 103 个,id 为 snake_case(tokyo, kuala_lumpur, ho_chi_minh, tbilisi…),数据在 [cities.json](../../app/src/main/assets/cities.json)。
- [CityRemoteRepository.kt](../../app/src/main/java/com/randomcity/app/data/remote/CityRemoteRepository.kt):`fetchCityImageUrl(englishName, imageKeywords)` 走 Wikipedia→Openverse→Bing 三级;无 context 依赖;调用方两处——[CityResultViewModel.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultViewModel.kt)(Hero)与 [CityImage.kt](../../app/src/main/java/com/randomcity/app/ui/components/CityImage.kt)(首页精选卡片 + Saved 行卡)。
- 开发机直连 Wikipedia/Openverse 均 200(已实测),批量下载可行。
- Coil 内置支持 `file:///android_asset/` 路径加载,无需额外依赖。

## Proposed Changes

### 1. 批量下载脚本(一次性工具)`.trae/dl-city-images.ps1`

对 103 个城市依次执行:
- **Wikipedia API**(`pithumbsize=1200`,gsrlimit=3 取首个有 thumbnail 的结果)→ 下载缩略图原文件;
- 失败 → **Openverse**(`q={name}&aspect_ratio=wide&per_page=5&filter_dead=true`,取首个 `thumbnail` 代理图,可再尝试 `url` 原图);
- 再失败 → 记入失败清单,脚本结束统一报告,人工补图或留空(运行时回落渐变)。
- **图像处理**(System.Drawing):按 16:9 居中裁剪 → 缩放至 800×450 → 透明底填白 → JPEG 质量 78 保存到 `app/src/main/assets/city_images/{cityId}.jpg`。
- 输出报告:成功数、来源分布(wiki/openverse)、失败清单、总大小。
- 速率:每城市间隔 ≥300ms,遵守 Wikipedia UA 政策。

### 2. 抽查图片内容(Read 工具)

抽查 ≥10 张(kuala_lumpur / kyoto / santorini / budapest / tbilisi / beijing / paris / new_york / zanzibar / queenstown + 随机),确认:① 内容对应该城市;② 无白边 letterbox;③ 非地图/徽章/广告。个别不理想的在脚本里用 Openverse 关键词(`imageKeywords` 或 `{name} skyline`)单独重下。

### 3. `CityRemoteRepository` 改造(本地优先)

```kotlin
class CityRemoteRepository(assets: AssetManager) {
    /** 内置城市图清单(assets/city_images/*.jpg)。 */
    private val bundledImages: Set<String> =
        assets.list("city_images")?.toSet() ?: emptySet()

    suspend fun fetchCityImageUrl(
        cityId: String,
        englishName: String,
        imageKeywords: List<String> = emptyList()
    ): String? {
        // 1. 内置 asset:离线、零延迟、必铺满(已精确裁剪 800x450)
        if ("$cityId.jpg" in bundledImages) {
            return "file:///android_asset/city_images/$cityId.jpg"
        }
        // 2. 远程兜底(未内置城市):Wikipedia → Openverse
        ...
    }
}
```

- **移除 Bing 源**(反复出广告/白边图;103 城市全内置后远程兜底极少触发)。
- `imageCache` 逻辑保留。

### 4. 调用方签名更新(3 处)

- [RandomCityApp.kt](../../app/src/main/java/com/randomcity/app/RandomCityApp.kt) L42:`CityRemoteRepository(context.applicationContext.assets)`。
- [CityResultViewModel.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultViewModel.kt) L59:传 `city.id` 为首参。
- [CityImage.kt](../../app/src/main/java/com/randomcity/app/ui/components/CityImage.kt) L23:传 `city.id` 为首参。

### 5. 清理

删除临时目录 `.trae/imgcheck/`;下载脚本执行后可保留在 `.trae/`(不入库,`.trae` 已在 git 忽略中则无需处理)。

### 6. 编译出 APK

`.\gradlew.bat assembleDebug`;报告 APK 体积(预计 assets 增加 ~8MB:103 张 × ~60-100KB)。

## Assumptions & Decisions

1. **APK 体积 +~8MB 可接受**——换来开/不开 VPN 都有图、秒加载、内容可控,是当前唯一 100% 可靠的方案。
2. **103 城市全覆盖**:个别下载失败的城市留空,运行时回落渐变(与现状一致),可后续人工补。
3. **Bing 源移除**而非保留:它已三次返回广告/白边图,留着只会带来不确定性。
4. 图片版权:Wikipedia/Openverse 均为 CC 授权图片,个人项目内置使用合规。
5. 脚本为一次性工具,放 `.trae/` 不纳入 app 代码。

## Verification

1. 脚本报告:成功 ≥100/103,失败清单为空或极少;总大小 ≤12MB。
2. 抽查 ≥10 张图片内容正确、无白边。
3. 编译通过,APK 体积增幅 ≈ 图片总量。
4. 真机验收(两种网络状态):
   - **飞行模式/断网**:详情页 Hero 有图且整幅铺满;首页精选卡片、Saved 行卡有图。
   - **开 VPN**:同上,全部有图。
   - 内容抽查:吉隆坡=双子塔类、京都=寺庙/天际线类、圣托里尼=蓝顶白房类。
