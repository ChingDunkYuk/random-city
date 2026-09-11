# Random City v0.8 计划(个人记录:Saved 升级 + History)

## 1. Summary

先提交并打 `v0.7.0` tag,然后按计划书§36–39 开发 v0.8:

- **Saved 升级**(§37):收藏页增加 搜索 / 排序 / 删除(删除已有)
- **History**(§38):新增历史记录(最近 100 城,cityId + viewedAt)
- **数据去重**(§39):已实现(最近 10 城排除),本轮仅把历史上限 20 → 100

版本号 0.8.0 / versionCode 6。

## 2. Current State Analysis

- [CityHistoryDao.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/local/CityHistoryDao.kt):insert / trimTo / recentIds 已有;缺"历史 + 城市信息"联合查询
- [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt):`HISTORY_KEEP = 20`,需提升到 100(§38 区间 50–100)
- [SavedScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt) / [SavedViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedViewModel.kt):仅列表 + 删除,无搜索/排序/历史
- 底部导航 v1.0 结构(§62)只有 Discover/Saved/Settings,History 不是独立 Tab → 收进 Saved 页顶部分段切换

## 3. 设计决策

| 决策 | 方案 | 说明 |
|---|---|---|
| History 入口 | Saved 页顶部分段切换:`收藏 | 历史`(两个 FilterChip) | 不新增底部 Tab,符合§62 结构 |
| 收藏搜索 | 顶部搜索框,匹配 中文名/英文名/国家(大小写不敏感) | 客户端过滤,数据量小 |
| 收藏排序 | 排序菜单:按收藏时间(新→旧,默认)/ 按名称(A→Z) | DropdownMenu + Sort 图标 |
| 历史展示 | 与收藏同款行卡 + 右侧浏览时间(M月d日 HH:mm),点击进详情 | 空态"还没有浏览记录" |
| 历史上限 | `HISTORY_KEEP = 100` | §38 区间上限,去重仍用最近 10(§39,不变) |

## 4. Proposed Changes(文件级)

### 4.1 [CityHistoryDao.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/local/CityHistoryDao.kt)
- 新增 `data class HistoryWithCity(@Embedded val city: CityEntity, val viewedAt: Long)`
- 新增查询:
  ```kotlin
  @Query("SELECT c.*, h.viewedAt AS viewedAt FROM cities c
          INNER JOIN city_history h ON c.id = h.cityId
          ORDER BY h.viewedAt DESC LIMIT :limit")
  fun observeHistoryWithCity(limit: Int): Flow<List<HistoryWithCity>>
  ```

### 4.2 [CityRepository.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/data/repository/CityRepository.kt)
- `HISTORY_KEEP` 20 → 100
- 新增 `data class HistoryItem(val city: City, val viewedAt: Long)`(domain 侧返回)
- 新增 `fun observeHistory(): Flow<List<HistoryItem>>`:DAO 映射 toDomain

### 4.3 [SavedViewModel.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedViewModel.kt)
- 注入 `CityRepository`(新增 history 流)
- `searchQuery: MutableStateFlow<String>`、`sortOrder: MutableStateFlow<SavedSort>`(TIME_DESC / NAME_ASC)
- `displayedSaved: StateFlow<List<City>>`:combine(saved, query, sort) 过滤 + 排序
- `history: StateFlow<List<HistoryItem>>`
- `remove(cityId)` 保留

### 4.4 [SavedScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt)
- 标题行:"收藏" + Sort 图标按钮(DropdownMenu:按收藏时间/按名称)
- 分段切换:收藏 | 历史(FilterChip ×2)
- 收藏视图:OutlinedTextField 搜索框(Icons.Outlined.Search,placeholder "搜索城市或国家")+ 现有行卡列表(数据源换 displayedSaved)
- 历史视图:行卡(复用 SavedCityRow 结构,右侧时间文本 `M月d日 HH:mm`,SimpleDateFormat("M月d日 HH:mm", Locale.CHINA)),空态"还没有浏览记录";点击进详情(历史行不显示删除按钮)

### 4.5 版本号
[build.gradle.kts](file:///d:/Random%20city/app/build.gradle.kts):versionCode 6 / versionName "0.8.0"

## 5. 执行顺序

1. commit + tag `v0.7.0`
2. DAO/Repository/ViewModel 改造
3. SavedScreen 重构(分段 + 搜索 + 排序 + 历史)
4. 版本号 → assembleDebug + testDebugUnitTest → 用户验收 → tag `v0.8.0`

## 6. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,26/26 单测保持全绿
2. 人工验收:
   - 收藏 3+ 城市后:搜索"东京"只留匹配项;切换排序顺序正确变化
   - 连抽若干城 → 历史 Tab 按时间倒序展示,时间格式正确;点行进详情
   - 杀进程重启:收藏与历史均在
   - 收藏删除正常;历史行无删除按钮
