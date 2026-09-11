# Random City v0.6 计划(Filters + Surprise Me)

## 1. Summary

先提交当前 UI 修复并打 `v0.5.0` tag,然后按计划书§30–32/§71 开发 v0.6:

- **Random Filters**:Continent / Travel Style / Budget 三维筛选,点击 Random 前可打开
- **Random Engine v2**:城市池 → 应用筛选 → 排除最近 10 城 → 随机;候选不足自动放宽
- **Surprise Me**:完全随机模式,忽略全部筛选(城市盲盒)

版本号 0.6.0 / versionCode 4。

## 2. Current State Analysis

- [RandomEngine.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/domain/RandomEngine.kt):`pick(cityIds, excludeIds, random)` 签名已支持排除集合,空池回退全量——无需改动
- [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt):`rollRandomCity()` 当前无筛选、仅排除 lastCityId
- [CityHistoryDao.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/local/CityHistoryDao.kt):`recentIds(limit)` 已存在,可直接取最近 10
- [SettingsRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/SettingsRepository.kt):DataStore 框架在,加 3 个 key 即可持久化筛选
- [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt):Random 按钮下方有空间放次级按钮行
- City 数据:continent/tags(TravelTag 9 种)/budgetLevel(1–4) 全部入库,103 城市内存过滤成本可忽略

## 3. 设计决策

| 决策 | 方案 | 说明 |
|---|---|---|
| 筛选交互 | ModalBottomSheet + M3 FilterChip | 计划书§30"点击 Random 前打开 Filters";FilterChip 自带选中态,符合柔和卡片风 |
| 选择模型 | 三个维度均**多选**;某维全不选 = 该维不限制 | 维度内 OR、维度间 AND |
| 筛选持久化 | DataStore 三个 key | 重启后保留用户偏好 |
| Surprise Me | 首页次按钮,忽略三维筛选,仍排除最近 10 城 | 计划书§32:排除重复属于防重而非筛选 |
| 最近排除 | 最近 10 城(historyDao.recentIds(10)) | 计划书§31;引擎现有回退逻辑自动放宽 |
| 空池处理 | 不跳转,首页提示"没有匹配的城市,试试放宽筛选" | 候选为空不报错(计划书§71) |
| Filters 入口状态 | 筛选激活时 Filters 按钮显示主色圆点 | 用户可见当前处于筛选模式 |

## 4. Proposed Changes(文件级)

### 4.1 domain:新增 [CityFilter.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/domain/model/CityFilter.kt)
```kotlin
data class CityFilter(
    val continents: Set<Continent> = emptySet(),
    val styles: Set<TravelTag> = emptySet(),
    val budgets: Set<Int> = emptySet()
) {
    val isActive: Boolean
    fun matches(city: City): Boolean  // 维度内 OR、维度间 AND
}
```
纯 Kotlin,JVM 可测。

### 4.2 [CityDao.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/local/CityDao.kt)
新增 `@Query("SELECT * FROM cities") suspend fun getAll(): List<CityEntity>`(103 行,内存过滤)。

### 4.3 [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt)
- `getFilteredIds(filter: CityFilter): List<String>`:getAll() → toDomain → filter.matches → id
- `rollRandomCity(filter: CityFilter, surprise: Boolean)`:
  - pool = surprise 或无筛选 → getAllIds();否则 getFilteredIds(filter);pool 为空 → null
  - exclude = historyDao.recentIds(10).toSet()
  - RandomEngine.pick(pool, exclude) → 写历史 + lastCityId(原逻辑保留)
- 签名变化后同步两个 ViewModel 调用点

### 4.4 [SettingsRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/SettingsRepository.kt)
- `cityFilter: Flow<CityFilter>` + `setCityFilter(filter)`:三个 key( continents/styles 存枚举名逗号串,budgets 存数字逗号串)

### 4.5 [HomeViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeViewModel.kt)
- `filter: StateFlow<CityFilter>`(来自 SettingsRepository)
- `onRandom(onPicked)` → `rollRandomCity(filter, surprise = false)`
- `onSurprise(onPicked)` → `rollRandomCity(filter, surprise = true)`
- `setFilter(CityFilter)` 持久化
- `emptyResult: StateFlow<Boolean>`:筛选池为空时置位,UI 提示后复位

### 4.6 新增 [FilterSheet.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/FilterSheet.kt)
`ModalBottomSheet` 内容:
- Section "大洲":6 个 FilterChip(ASIA→欧洲等中文 label)
- Section "旅行风格":9 个 FilterChip(TravelTag.label)
- Section "预算":4 个 FilterChip($/$$/$$$/$$$$)
- 底部:`重置` TextButton + `完成` 主按钮
样式复用主题(圆角 50 chips、SectionTitle)。

### 4.7 [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)
- Random 按钮下新增次级按钮行:
  - `OutlinedButton`:"Filters"(Icons.Outlined.FilterList;筛选激活时图标旁 8dp 主色圆点)
  - `OutlinedButton`:"Surprise Me"(Icons.Outlined.AutoAwesome→若不存在则用 Icons.Outlined.Shuffle)
- 点击 Filters → 打开 ModalBottomSheet(状态在 HomeScreen:showSheet)
- `emptyResult` 为 true 时显示提示文本(Snackbar 或按钮下方小字)

### 4.8 测试
新增 [CityFilterTest.kt](file:///d:/Random%20city/app/src/test/java/com/randomcity/app/CityFilterTest.kt):
- 空筛选匹配全部
- 单维筛选(洲/风格/预算)各自命中与排除
- 多维 AND 组合
- 维度内多值 OR

### 4.9 版本号
[build.gradle.kts](file:///d:/Random%20city/app/build.gradle.kts):versionCode 4 / versionName "0.6.0"

## 5. 执行顺序

1. commit 当前 UI 修复 + tag `v0.5.0`
2. CityFilter + 单测
3. DAO/Repository/Settings 改造
4. HomeViewModel + FilterSheet + HomeScreen
5. 版本号 → assembleDebug + testDebugUnitTest → 用户验收 → tag `v0.6.0`

## 6. Verification

1. `gradlew.bat testDebugUnitTest`:全部单测通过(20 现有 + CityFilter 新增)
2. `gradlew.bat assembleDebug` 编译通过
3. 人工验收:
   - 筛选 亚洲+美食 → 连续 Random 结果均为亚洲且带 FOOD 标签城市
   - 筛选重启 App 后保留;Filters 按钮有激活圆点
   - Surprise Me 可抽到与筛选无关的城市(如筛选非洲时抽出欧洲)
   - 极端筛选(如 大洋洲+$$$$ 无交集)不跳转并提示
   - 随机结果不重复最近 10 城
