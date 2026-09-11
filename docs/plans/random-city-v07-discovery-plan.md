# Random City v0.7 计划(Discovery 首页升级)

## 1. Summary

先提交并打 `v0.6.0` tag,然后按计划书§33–35 开发 v0.7:

- **Explore by Mood**(§34):首页增加 5 个心情入口(美食之旅/海岛/摄影/历史/自然),点击即"该风格筛选 + 直接开抽"
- **Featured Cities**(§35):首页增加 3–5 个静态运营推荐城市横滑卡片,点击进城市详情

首页从"单按钮页"升级为 Discovery 页(可滚动)。版本号 0.7.0 / versionCode 5。

## 2. Current State Analysis

- [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt):标题+副文案+地球(220dp+290dp 底环)+Random+筛选/惊喜次按钮+提示文字,Column 不可滚动
- [HomeViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeViewModel.kt):已有 `rollRandomCity(filter, surprise)`、`setFilter`、`filter: StateFlow<CityFilter>` —— Mood 入口可直接复用
- [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt):有 `getCity(id)`,Featured 只需静态 id 列表
- [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt):渐变封面可直接用于 Featured 卡片
- 导航:`onCityPicked(cityId)` 已能跳详情(主按钮链路),Featured 卡片复用同一回调即可

## 3. 设计决策

| 决策 | 方案 | 说明 |
|---|---|---|
| Mood 列表 | 美食之旅/海岛/摄影/历史/自然 5 个(§34 原样) | 对应 TravelTag FOOD/BEACH/PHOTOGRAPHY/HISTORY/NATURE |
| Mood 行为 | 覆盖 styles 维(保留洲/预算维)+ 立即开抽 | §34"Filter=Photography → Random" |
| Mood 图标 | Material 线性图标(去 emoji):Restaurant/BeachAccess/PhotoCamera/AccountBalance/Park | 延续图标化原则 |
| Featured 名单 | 京都、圣托里尼、第比利斯、皇后镇、马拉喀什(静态常量) | 五大洲风格各异、视觉感强;§35 明确"非算法,运营静态推荐" |
| Featured 卡片 | 110×140dp 横滑卡:渐变封面 + 城市名(中/英)+ 国家 | 复用 CityVisuals |
| 首页布局 | 改可滚动:Hero 区(地球缩至 170dp,去掉 290dp 底环)→ Random → 次按钮 → 按心情探索 → Featured | 内容增加必须滚动;Hero 压缩保住核心按钮位置(§3.1 中下区域) |

## 4. Proposed Changes(文件级)

### 4.1 [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt)
- 新增 `suspend fun getFeaturedCities(): List<City>`:静态 id 列表(kyoto/santorini/tbilisi/queenstown/marrakech)→ getCity 映射,过滤 null
- `companion object { val FEATURED_IDS = listOf(...) }`

### 4.2 [HomeViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeViewModel.kt)
- `featured: StateFlow<List<City>>`:init 中加载 getFeaturedCities()
- `onMood(tag: TravelTag, onPicked: (String) -> Unit)`:`setFilter(filter.value.copy(styles = setOf(tag)))` 后复用 `roll(surprise = false, onPicked)`

### 4.3 [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)
- 整体改 `verticalScroll` Column;地球 220→170dp,删 290dp 底环
- Random/次按钮/提示文案保持不变(位置随滚动)
- 新增"按心情探索" Section:5 个 Mood 卡(FlowRow 或等宽两行),卡片内容:图标 + 中文名;点击 → `viewModel.onMood(tag, onCityPicked)`
- 新增"精选城市" Section:`LazyRow` Featured 卡(渐变 + localName + name + countryLocal),点击 → `onCityPicked(city.id)`

### 4.4 版本号
[build.gradle.kts](file:///d:/Random%20city/app/build.gradle.kts):versionCode 5 / versionName "0.7.0"

## 5. 执行顺序

1. commit + tag `v0.6.0`
2. Repository/ViewModel 改造
3. HomeScreen 滚动化 + 两个新 Section
4. 版本号 → assembleDebug + testDebugUnitTest → 用户验收 → tag `v0.7.0`

## 6. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,26/26 单测保持全绿
2. 人工验收:
   - 首页可滚动,Random 按钮与筛选/惊喜按钮位置顺手
   - 点"海岛":直接抽到带 BEACH 标签城市;Filters 圆点激活(styles 维)
   - Featured 横滑正常,点卡片进对应城市详情
   - 空池筛选下 Mood 行为正常(提示放宽)
