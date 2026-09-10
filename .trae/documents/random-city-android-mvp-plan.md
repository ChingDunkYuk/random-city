# Random City Android — 开发计划(MVP v0.1 → v1.0 路线)

## 1. Summary

依据《Random City Android 项目计划书》在空目录 `d:\Random city` 从零搭建 Android 项目。
本轮交付 **MVP v0.1**(验证核心循环:Random → 城市揭晓 → 快速了解 → 收藏 → 继续 Random),
架构与数据模型按 v1.0 终态设计,避免后续阶段重构。后续 v0.3–v1.0 各 Phase 在路线图中列出,不在本轮实施。

## 2. Current State Analysis

- **工作目录**:`d:\Random city` 为空目录(全新项目,无历史代码约束)。
- **构建环境**(已探测):
  - JDK:OpenJDK 21.0.11 (Temurin) ✓
  - Android SDK:`C:\Android\Sdk`,含 platforms `android-34` / `android-36`、build-tools `30.0.3`/`34.0.0`/`35.0.0`、platform-tools(adb)、cmdline-tools ✓
  - `ANDROID_HOME` 未设置;系统级 Gradle 未安装 → 需引导 Gradle Wrapper(下载发行版,需网络)
  - 无 emulator system-images → 验证以编译 + JVM 单元测试为主,真机需用户自行连接 adb
- **约束**:Windows + PowerShell;构建时通过 `local.properties` 指向 `C:\Android\Sdk`。

## 3. Assumptions & Decisions(用户跳过的提问 → 本计划采用的默认值)

| 决策点 | 采用方案 | 理由 |
|---|---|---|
| 本轮范围 | 仅 MVP v0.1 | 计划书原则"MVP 坚决不堆功能";先验证核心循环 |
| UI 语言 | 中文为主,城市名中英对照(东京 / Tokyo) | 用户中文沟通;计划书示例为中英混排 |
| 城市数据规模 | 约 100 城市,字段完整 | 平衡数据编写成本与随机池丰富度;结构支持扩到 300 |
| 视觉风格 | Material 3 干净中性风,支持 Light/Dark/System | 用户记忆:拒绝粉色/暖色/深空黑/星场动效;图标用 Material Symbols 风格 |
| 城市封面图(MVP) | 本地程序化渐变/图案占位 Header,不联网 | 计划书将远程图片(Coil)划在 v0.9;MVP 保持完全离线、APK 轻量 |
| DI 方案 | 手动依赖注入(AppContainer + ViewModelFactory) | 计划书要求"不需要过度复杂";不引 Hilt |

## 4. 技术栈(锁定版本)

- Kotlin 2.0.21 + AGP 8.7.3 + Gradle Wrapper 8.10.2(JDK 21 兼容)
- compileSdk 36 / minSdk 28 / targetSdk 36(对应计划书 Android 9+ 要求)
- Jetpack Compose(BOM 2024.12.01)+ Material 3 + Navigation Compose 2.8.x
- Room 2.6.1(KSP 2.0.21-1.0.x)+ kotlinx-serialization-json 1.7.x
- DataStore Preferences(用户设置:lastCityId、主题)
- Lifecycle ViewModel Compose 2.8.x + Kotlin Coroutines/Flow
- 测试:JUnit4 + kotlinx-coroutines-test(RandomEngine JVM 单测)

## 5. Proposed Changes(文件级)

### 5.1 项目骨架

```text
d:\Random city\
├─ settings.gradle.kts                  # pluginManagement: google()/mavenCentral
├─ build.gradle.kts                     # 根插件声明(不 apply)
├─ gradle.properties                    # AndroidX、非传递 R 类、JVM args
├─ local.properties                     # sdk.dir=C:\\Android\\Sdk(本机)
├─ gradle/wrapper/gradle-wrapper.properties  # gradle-8.10.2-bin
├─ gradlew / gradlew.bat                # 由下载的 Gradle 发行版生成
└─ app/
   ├─ build.gradle.kts                  # applicationId com.randomcity.app
   └─ src/main/
      ├─ AndroidManifest.xml            # 仅 INTERNET 权限(计划书§60)
      ├─ assets/cities.json             # ~100 城市完整数据
      └─ java/com/randomcity/app/
         ├─ RandomCityApp.kt            # Application + AppContainer(手动 DI)
         ├─ MainActivity.kt             # 单 Activity,Compose 入口
         ├─ data/
         │  ├─ local/
         │  │  ├─ CityDatabase.kt       # Room:cities/saved_cities/city_history
         │  │  ├─ CityDao.kt            # 城市查询(含随机候选查询)
         │  │  ├─ SavedCityDao.kt       # 收藏增删查
         │  │  ├─ CityHistoryDao.kt     # 最近浏览(上限 20,MVP)
         │  │  ├─ entity/CityEntity.kt
         │  │  ├─ entity/SavedCityEntity.kt
         │  │  └─ entity/CityHistoryEntity.kt
         │  ├─ model/CityDto.kt         # JSON 反序列化模型(@Serializable)
         │  ├─ repository/CityRepository.kt      # JSON→Room 导入 + 读取
         │  ├─ repository/SavedRepository.kt
         │  └─ repository/HistoryRepository.kt
         ├─ domain/
         │  ├─ model/City.kt            # UI 层领域模型(完整字段,见§6)
         │  └─ RandomEngine.kt          # 纯函数随机引擎(可单测)
         ├─ ui/
         │  ├─ theme/                   # Color/Type/Theme(Light+Dark)
         │  ├─ splash/SplashScreen.kt + SplashViewModel.kt
         │  ├─ home/HomeScreen.kt + HomeViewModel.kt
         │  ├─ city/CityResultScreen.kt + CityResultViewModel.kt
         │  ├─ saved/SavedScreen.kt + SavedViewModel.kt
         │  └─ components/              # CityHeader(渐变占位)、QuickInfoCard、TagChip 等
         ├─ navigation/NavGraph.kt      # Splash → Main(Discover/Saved) → CityResult
         └─ util/CityVisuals.kt         # 由城市 id 生成确定性渐变配色
   └─ src/test/java/com/randomcity/app/RandomEngineTest.kt
```

### 5.2 City 数据模型(按 v1.0 终态设计,MVP UI 只用子集)

`assets/cities.json` 每个城市字段(对应计划书§6/§53 完整性要求):

```json
{
  "id": "tokyo",
  "name": "Tokyo",
  "localName": "东京",
  "country": "Japan",
  "countryLocal": "日本",
  "countryCode": "JP",
  "continent": "ASIA",
  "latitude": 35.6762,
  "longitude": 139.6503,
  "description": "一座未来感、传统文化与庞大城市生活交织在一起的超级都市。",
  "tags": ["CITY", "FOOD", "NIGHTLIFE"],
  "recommendedDaysMin": 4,
  "recommendedDaysMax": 6,
  "bestMonths": [3, 4, 5, 10, 11],
  "budgetLevel": 3,
  "transportScore": 5,
  "attractions": ["涩谷", "浅草", "新宿", "明治神宫", "东京晴空塔"],
  "foods": ["寿司", "拉面", "炸猪排", "烤鸡串", "天妇罗"],
  "stayAreas": [{"name": "新宿", "note": "交通方便,适合第一次来东京"}],
  "transportTips": ["Metro", "Train", "Walk"],
  "travelTips": [{"category": "PAYMENT", "text": "交通卡 Suica 通用"}],
  "route": [{"day": 1, "stops": ["涩谷", "原宿", "明治神宫", "新宿"]}],
  "imageKeywords": ["tokyo", "shibuya"]
}
```

- 必填(MVP 校验):id/name/localName/country/continent/description/tags/recommendedDays/bestMonths/attractions/foods
- 选填(stayAreas/transportTips/travelTips/route):为 v0.3/v0.5 预留,Room 中以 JSON 字符串列存储(TypeConverter),后续版本仅加 UI 不改库
- 城市清单策略:亚洲 ~35(计划书§13 列出的全部 + 补充)、欧洲 ~35、美洲/非洲/大洋洲 ~30
- 首次启动:JSON → 校验 → 写入 Room(`schemaVersion` 记录,数据升级时重导)

### 5.3 RandomEngine(domain 层,纯 Kotlin,JVM 可测)

```text
pick(candidates, excludeIds): City?
```

- MVP 规则:从全量城市池排除 `lastCityId` 后等概率随机(计划书§12)
- 接口预留 `excludeIds: Set<String>`,v0.6 filters / v0.8 recent-10 只需扩展入参,不改引擎签名
- 单测覆盖:不重复、空池、单候选、排除后为空等边界

### 5.4 页面与导航(MVP 三页 + 底部导航)

1. **Splash**:Logo "Random City" + 加载城市库/设置(≤2s)→ 自动进 Main
2. **Main(Discover Tab)**:标题 "Random City" + 副文案 "Where should we go next?" + 程序化渐变城市视觉 + 超大 **🎲 Random** 按钮(屏幕中下区域,单手可达,计划书§3.1)
3. **City Result**:渐变 Cover Header(城市名中英 + 国家 + 标签)→ 一句话简介 → Quick Info 四卡(推荐天数/最佳月份/预算/交通评分)→ 核心景点 3–5 → 当地美食 3–5 → ♡ Save 切换 + 底部大按钮 **🎲 Random Again**(计划书§17)
4. **Main(Saved Tab)**:收藏列表(城市名/国家/标签),点击进入详情,左滑或长按删除;空态引导文案
5. **导航**:Splash 单页;Main 内 Bottom Navigation(Discover/Saved);City Result 全屏 push,Random Again 替换当前城市不产生返回栈堆积(launchSingleTop + 状态替换)

### 5.5 数据流

```text
Splash 启动
  ├─ DataStore 读 schemaVersion → 不匹配则 cities.json → Room(后台 Dispatchers.IO)
  └─ 完成后进 Main
Random 点击
  └─ HomeViewModel → CityRepository.getAllIds → RandomEngine.pick(排除 lastCityId)
      → CityResultScreen(cityId) → Repository.getCity → 写入 city_history + 更新 lastCityId
Save 点击
  └─ SavedRepository.toggle(cityId) → saved_cities 表;重启后仍在(验收§21)
```

## 6. 构建引导步骤(执行阶段第一步)

1. 下载 Gradle 8.10.2 发行版至临时目录并执行 `gradle wrapper --gradle-version 8.10.2` 生成 Wrapper(需网络)
2. 写 `local.properties` 指向 `C:\Android\Sdk`
3. `gradlew.bat assembleDebug` 验证编译;后续全部用 Wrapper

## 7. Roadmap(本轮之后,不在本次实施)

| 版本 | 内容 | 与 MVP 的衔接 |
|---|---|---|
| v0.3 | 住宿区域/交通/旅行贴士 UI | 数据字段已入库,仅加 City Detail Section |
| v0.5 | Suggested Route + Timeline UI | `route` 字段已预留 |
| v0.6 | Filters + Surprise Me | RandomEngine 入参已预留 excludeIds;加 filter 维度查询 |
| v0.7 | Explore by Mood / Featured | 首页 Section 扩展 |
| v0.8 | Saved 搜索排序 + History 页(50–100 条) | city_history 表 MVP 已建,提上限即可 |
| v0.9 | Coil 远程图片 / 天气 / 外部跳转(地图/机酒/餐饮)/ Share | `imageKeywords`、经纬度字段已预留 |
| v0.9.5 | Random 洗牌动画 / Reveal 动画 / 震动反馈 | UI 层增量 |
| v1.0 RC | R8(keep 规则:serialization/Room)、性能、离线、数据完整性测试、Release 签名 | 见计划书§51–58 |

## 8. Verification(本轮验收,对应计划书§21)

1. `gradlew.bat assembleDebug` 编译通过,无警告升级错误
2. `gradlew.bat testDebugUnitTest`:RandomEngine 单测全绿(不连续重复/空池/单元素)
3. Lint 基础检查通过(`gradlew.bat lintDebug`,不阻断级别)
4. 数据校验脚本(单测):cities.json 必填字段完整、id 唯一、≥100 城市
5. 手动验收清单(真机/模拟器,由用户执行):
   - 冷启动 ≤2s 进入首页;Random → 城市出现 ≤1s
   - 连续 Random 不出现连续重复城市
   - 城市详情各 Section 显示完整;♡ 收藏/取消正常
   - 杀进程重启后收藏仍在;断网全程可用
   - Dark Mode 下 Header 蒙层/卡片/底部导航显示正常

## 9. 风险与备注

- **Gradle Wrapper 引导需网络**:下载发行版失败时退化为提示用户安装 Gradle 或提供离线路径
- **无模拟器**:本环境只能保证编译 + JVM 测试;UI 走查需用户真机(adb 已具备)
- **100 城市数据编写**:内容质量直接影响"继续抽的欲望",按§5.2 清单均衡分布六大洲,描述文案保持一句话、有画面感
- 严格不引入计划书§68 排除项(登录/云同步/定位/推送等)
