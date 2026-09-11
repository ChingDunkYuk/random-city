# Random City v0.9 计划(联网能力:图片 / 天气 / 外部跳转 / 分享)

## 1. Summary

先提交并打 `v0.8.0` tag,然后按计划书§40–44 开发 v0.9:

- **城市远程图片**(§41):Coil 加载,失败/离线回落渐变封面
- **当前天气**(§42):Optional Section,API 失败不影响城市页
- **外部跳转**(§43):地图 / 机票 / 酒店 / 餐饮
- **分享城市**(§44):Android Share Sheet

全部选用**无需 API Key** 的服务,离线原则不变(§3.4:联网失败核心功能不受影响)。
版本号 0.9.0 / versionCode 7。

## 2. Current State Analysis

- [CityResultViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultViewModel.kt):init 加载城市;可加并行加载图片/天气
- [CityResultScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt):Hero 为渐变 Box;顶部操作栏只有返回 + 收藏
- [AndroidManifest.xml](file:///d:/Random%20city/app/src/main/AndroidManifest.xml):INTERNET 权限已声明(§60 唯一权限)
- City 数据:`name`(英文)、`latitude/longitude` 齐全,满足图片搜索与天气查询
- [AppContainer](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/RandomCityApp.kt):手动 DI,新增 remote 仓库在此注册

## 3. 技术选型(全部免 API Key)

| 能力 | 服务 | 说明 |
|---|---|---|
| 城市图片 | Wikipedia PageImages API | `en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch={name}&gsrlimit=1&prop=pageimages&pithumbsize=800&format=json`,城市词条代表图,稳定免费;需自定义 User-Agent |
| 天气 | Open-Meteo | `api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current_weather=true`,免 key,WMO weathercode → 中文描述 |
| 网络层 | OkHttp 4.12.0 + 手动 JsonElement 解析 | 仅 2 个小端点,不引 Retrofit;无序列化类,R8 安全 |
| 图片加载 | Coil 2.7.0(coil-compose) | 内存/磁盘缓存、懒加载(§41) |

## 4. 设计决策

| 决策 | 方案 |
|---|---|
| 图片展示 | Hero 保持渐变兜底;imageUrl 加载成功后 AsyncImage 覆盖(ContentScale.Crop);失败/离线 = 渐变(§41) |
| 图片 URL 缓存 | 内存 LruCache(50)+ 成功才存;不持久化,避免失效链接 |
| 天气位置 | Quick Info 卡下方一行小卡:云朵图标 + "现在 26°C · 多云";加载失败整条隐藏(§42) |
| 外部链接 | 城市页底部"出行链接" Section:4 个图标按钮(地图 Map / 机票 Flight / 酒店 Hotel / 餐饮 Restaurant),ACTION_VIEW intent |
| 链接 URL | 地图 `geo:lat,lng?label`(回退浏览器 google.com/maps);机票 Google Flights `google.com/travel/flights?q=flights to {name}`;酒店 `booking.com/searchresults.html?ss={name}`;餐饮 `google.com/maps/search/restaurants+in+{name}` |
| 分享 | Header 顶部操作栏加分享图标(收藏左侧),ACTION_SEND 纯文本(中文格式,见§5.6) |
| 天气文案 | WMO code 映射:0 晴 / 1–3 多云·阴 / 45,48 雾 / 51–57 毛毛雨 / 61–67 雨 / 71–77 雪 / 80–82 阵雨 / 85–86 阵雪 / 95–99 雷雨 |

## 5. Proposed Changes(文件级)

### 5.1 依赖与清单
- [build.gradle.kts](file:///d:/Random%20city/app/build.gradle.kts):`io.coil-kt:coil-compose:2.7.0`、`com.squareup.okhttp3:okhttp:4.12.0`

### 5.2 新增 data/remote/CityRemoteRepository.kt
- `suspend fun fetchCityImageUrl(englishName: String): String?`:Wikipedia API,OkHttp 同步请求 + `withContext(Dispatchers.IO)`,JsonElement 手动取 `query.pages[*].thumbnail.source`;异常/无结果 → null;带 User-Agent 头
- `suspend fun fetchCurrentWeather(lat: Double, lng: Double): CurrentWeather?`:Open-Meteo,取 `current_weather.temperature + weathercode`;异常 → null
- `data class CurrentWeather(val temperatureC: Int, val description: String)`:weathercode → 中文(§4 映射)
- 内存 LruCache<String, String> 缓存图片 URL

### 5.3 [RandomCityApp.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/RandomCityApp.kt)
- AppContainer 注册 `cityRemoteRepository`

### 5.4 [CityResultViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultViewModel.kt)
- 注入 `CityRemoteRepository`
- `imageUrl: StateFlow<String?>`、`weather: StateFlow<CurrentWeather?>`:init 中城市加载成功后并行 async 拉取(各独立 try/catch,失败留 null)

### 5.5 [CityResultScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt)
- Hero:渐变 Box 之上叠加 `AsyncImage`(imageUrl 非空时),底部蒙层保持
- 顶部操作栏:收藏按钮左侧新增分享图标(`Icons.Outlined.Share`)→ 构造分享文本并 `ACTION_SEND`(经 `LocalContext`,createChooser)
- Quick Info 下新增天气行(weather != null 时):`Icons.Outlined.Cloud` + "现在 {t}°C · {desc}"
- 底部(旅行贴士后)新增"出行链接" Section:4 个 OutlinedButton 小卡(Map / Flight / Hotel / Restaurant 图标 + 文字),点击 fire intent(见§4 URL 规则)

### 5.6 分享文本(中文,对应§44 结构)
```text
我在 Random City 发现了 东京 Tokyo 🌏
日本 Japan
美食 · 都市 · 夜生活
建议游玩:4–6 天
```

### 5.7 版本号
versionCode 7 / versionName "0.9.0"

## 6. 执行顺序

1. commit + tag `v0.8.0`
2. 依赖 + CityRemoteRepository + AppContainer
3. CityResultViewModel 加载图片/天气
4. CityResultScreen:图片 Hero / 分享 / 天气行 / 出行链接
5. 版本号 → assembleDebug + testDebugUnitTest → 用户验收 → tag `v0.9.0`

## 7. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,26/26 单测保持全绿
2. 人工验收:
   - 联网:城市页顶部显示真实城市照片(Wikipedia),连抽多城图片各异
   - 断网:图片位回落渐变,天气行隐藏,其余功能正常(§3.4 离线原则)
   - 天气行显示"现在 x°C · 天气"或整体隐藏(失败时)
   - 4 个出行按钮分别唤起地图/浏览器对应页面
   - 分享唤起系统分享面板,文本格式正确
