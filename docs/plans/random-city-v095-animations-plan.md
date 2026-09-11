# v0.9.0 打 tag + v0.9.5 洗牌动画 / Reveal 动画 / 震动反馈

## Summary

v0.9.0 验收通过,先提交打 tag;然后按主计划书 Roadmap(L172)开发 **v0.9.5**:
Random 洗牌动画 / Reveal 动画 / 震动反馈(UI 层增量)。

约束(用户记忆):拒绝**持续环境动效**(脉冲/呼吸/辉光);本轮全部是**一次性交互动效**,克制不花哨。

## Current State Analysis

- 当前版本:`versionCode=7 / versionName="0.9.0"`([build.gradle.kts](../../app/build.gradle.kts) L17-18)。
- 待提交:9 个修改文件 + 未跟踪的 `app/src/main/assets/city_images/`(103 张,构建必需)、`app/src/main/java/com/randomcity/app/data/remote/`、`app/src/main/java/com/randomcity/app/ui/components/CityImage.kt`。
- git 习惯:仅 `app/` 代码与主计划书入库;`.trae/*.ps1`、各阶段 plan 文档、`.kotlin/` 不提交。
- [HomeViewModel.kt](../../app/src/main/java/com/randomcity/app/ui/home/HomeViewModel.kt) `roll()`:抽取后立即 `onPicked` 导航,无动画窗口;已注入 `cityRepository`(`getFilteredIds`/`cityDao.getAll()` 可提供城市名)。
- [CityResultScreen.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt):`uiState.loading=false` 后内容一次性呈现,无进入动画。
- 震动:用 Compose `LocalHapticFeedback`(系统视图反馈,**无需 VIBRATE 权限**,符合计划书§60 仅 INTERNET)。

## Proposed Changes

### 1. 提交 v0.9.0 + tag

```text
git add app/build.gradle.kts app/src/main/java/... (9 个修改文件)
        app/src/main/assets/city_images/
        app/src/main/java/com/randomcity/app/data/remote/
        app/src/main/java/com/randomcity/app/ui/components/CityImage.kt
git commit -m "feat: v0.9.0 联网增强:天气/分享/出行链接 + 103 城图片内置化"
git tag v0.9.0
```

### 2. 版本号 — [build.gradle.kts](../../app/build.gradle.kts)

`versionCode = 8`,`versionName = "0.9.5"`。

### 3. 洗牌动画 — HomeViewModel + HomeScreen

**[HomeViewModel.kt](../../app/src/main/java/com/randomcity/app/ui/home/HomeViewModel.kt)**:

```kotlin
/** 洗牌滚动序列(最后一个是抽中城市),空表=不在洗牌。 */
private val _shuffleNames = MutableStateFlow<List<String>>(emptyList())
val shuffleNames: StateFlow<List<String>> = _shuffleNames.asStateFlow()
```

`roll()` 流程改造:
1. `_rolling = true`
2. `picked = cityRepository.rollRandomCity(...)`(IO,快)
3. 非 null 时:从 `cityRepository.getFilteredIds(filter)`/全量 id 随机取 9 个 + picked 组成 10 项序列(需要去重且不含 picked;id→localName 通过 `getCity` 批量取,或 CityRepository 新增 `getNamesByIds(ids)`,**采用后者**:一次 DAO 查询返回 `List<String>`,避免 10 次单查)
4. `_shuffleNames.value = 序列`;`delay(SHUFFLE_DURATION_MS)`(约 950ms)
5. `_shuffleNames.value = emptyList(); _rolling = false; onPicked(picked)`

- [CityDao.kt](../../app/src/main/java/com/randomcity/app/data/local/CityDao.kt) 新增:`@Query("SELECT localName FROM cities WHERE id IN (:ids)") suspend fun getLocalNames(ids: List<String>): List<String>`;CityRepository 包装 `getLocalNamesByIds`。

**[HomeScreen.kt](../../app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)**:

- 收集 `shuffleNames`;非空时在**吉祥物与"随机"按钮之间**显示固定高度(56dp)的滚动城市名:
  ```kotlin
  var displayName by remember { mutableStateOf("") }
  LaunchedEffect(shuffleNames) {
      shuffleNames.forEachIndexed { i, name ->
          displayName = name
          delay(60L + i * 6L)  // 轻微减速,10 项 ≈ 950ms
      }
  }
  AnimatedContent(targetState = displayName, transitionSpec = {
      (slideInVertically { it / 2 } + fadeIn(tween(90))) togetherWith
          (slideOutVertically { -it / 2 } + fadeOut(tween(90)))
  }, label = "shuffle") { Text(it, headlineSmall, Bold, primary) }
  ```
- 滚动时按钮保持禁用(现有 `enabled = !rolling` 已覆盖);序列播完 ViewModel 触发导航,首页状态随组合销毁。

### 4. Reveal 动画 — CityResultScreen

[CityResultScreen.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt):

- `city` 就绪后启动单次动画:
  ```kotlin
  val reveal = remember { Animatable(0f) }
  LaunchedEffect(city.id) { reveal.snapTo(0f); reveal.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
  ```
- 三组错峰(graphicsLayer,不触发重排):
  | 组 | 内容 | alpha 区段 | translationY |
  |---|---|---|---|
  | 1 | Hero(图片+标题) | 0→0.4 | 24dp→0 |
  | 2 | 简介 + QuickInfo + 天气行 | 0.25→0.65 | 20dp→0 |
  | 3 | 其余全部 section | 0.45→0.85 | 16dp→0 |
- 实现:每组包一层 `Modifier.graphicsLayer { alpha = ...; translationY = ... }`,插值函数 `private fun revealAt(progress: Float, start: Float, end: Float) = ((progress - start) / (end - start)).coerceIn(0f, 1f)`。
- 底部"再抽一个"按钮不动画(保持可操作稳定区)。

### 5. 震动反馈(LocalHapticFeedback,无权限)

- [HomeScreen.kt](../../app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt):`val haptic = LocalHapticFeedback.current`,Random / 惊喜一下 / 心情卡片点击时 `haptic.performHapticFeedback(HapticFeedbackType.LongPress)`。
- 洗牌定格:ViewModel `onPicked` 回调里(HomeScreen 的 `onCityPicked` lambda 包装处)再震一次 LongPress——"敲定"感。
- [CityResultScreen.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt):收藏切换时 LongPress 一次。

### 6. 编译出 APK

`.\gradlew.bat assembleDebug`,产物 `app/build/outputs/apk/debug/app-debug.apk`。

## Assumptions & Decisions

1. 洗牌动画**延迟导航约 950ms**——仪式感换等待,可接受;`rolling` 防抖已存在,连点安全。
2. 滚动序列 10 项(9 随机 + 1 结果),本地 DAO 取名,不联网。
3. Reveal 仅覆盖首屏三组,长列表 section 不做(避免滚动时闪烁感);Random Again 换新城市页会重新播放(每次揭晓都有仪式感)。
4. 震动统一 `LongPress`(API 兼容、语义明确);不加权限、不加设置开关(v0.9.5 范围外)。
5. `.trae/*.ps1`、阶段 plan 文档维持不入库惯例。

## Verification

1. `git tag` 输出含 `v0.9.0`;`git status` 仅剩 .trae/.kotlin 未跟踪。
2. `.\gradlew.bat assembleDebug` 编译通过。
3. 真机验收:
   - 点"随机":城市名在首页快速滚动减速(~1s)→ 震动 → 进入结果页;期间按钮禁用、无连点跳两次。
   - 结果页:Hero 淡入上滑 → QuickInfo 跟进 → 下方内容跟进,无突兀闪现。
   - 收藏/取消有震动;Random Again 换新城市同样洗牌+Reveal。
   - 深色模式下动画无异常;动画结束后无残留半透明/位移。
