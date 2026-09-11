# Random City UI 重构 v3 计划(Soft Cards 柔和卡片风)

## 1. Summary

v2(Jetsnack 风)被否:主色 #4C6FFF 与封面渐变过于鲜艳刺眼。
本次方向(用户明确):**更卡片、更浅色、更柔和**——
低饱和雾感蓝、浅灰底白卡、20dp 大圆角、柔和投影、雾感 pastel 渐变封面、字重收敛。
不改功能逻辑与数据层;完成后验收 v0.5.0。

## 2. Current State Analysis

现状(v2)问题:
- [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt):primary #4C6FFF 饱和度过高,观感"硬"
- [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt):6 组渐变明度低、饱和高(#2E7FD9/#2F3FA8 等),不够柔和
- 卡片 16dp 圆角 + 3dp 投影:圆角与阴影都偏"利落",缺柔和感
- SectionTitle titleLarge Bold + Space Grotesk:字重过硬

## 3. 设计决策

| 决策 | 方案 | 说明 |
|---|---|---|
| 色彩 | 低饱和雾感蓝 #6B8FE8 | 保留冷色系,饱和度大幅下降 |
| 背景 | #F6F7F9 柔浅灰 | 比纯白更柔 |
| 卡片 | 白色 + 20dp 圆角 + 4dp 柔和投影 | 卡片成为绝对视觉主体 |
| 封面 | 6 组雾感 pastel 渐变 + 白字 + 轻蒙层 | 明度提高、饱和降低 |
| 字重 | 标题 SemiBold(从 Bold 收敛) | 减少攻击性 |
| 字体 | 保留 Space Grotesk | v2 已接入,不变 |
| 深色模式 | 同步柔和化(非本次重点) | 深底 #141922 + 柔蓝 #9DB4F0 |

## 4. 设计令牌(锁定)

### 浅色(主方向)
```text
background      #F6F7F9
surface         #FFFFFF
primary         #6B8FE8   雾感蓝
primaryContainer#EAF0FD
onPrimaryContainer#44609F
secondary       #7BB8C4   雾青
secondaryContainer#E4F2F4
onSurface       #2A2E35   柔黑
onSurfaceVariant#7B818B
outline         #E9EBEF
卡片            20dp 圆角 + elevation 4dp
按钮            药丸(50%) + 柔蓝
```

### 深色(同步柔和)
```text
background      #141922
surface         #1E2530
primary         #9DB4F0
primaryContainer#2E3C60
onSurface       #E9ECF2
onSurfaceVariant#9AA1AE
outline         #2E3744
```

### 封面渐变(v3,6 组雾感 pastel,白字 + 20% 黑蒙层保持可读)
```text
#9DB4EA → #6E8FD6   雾蓝
#93C8D4 → #5E9FC9   雾青蓝
#A3A9E8 → #7079C8   雾靛
#8FBFC9 → #5B93C4   雾蓝绿
#AAB6EE → #7C88D4   柔紫蓝
#9CC3E8 → #6B96D6   雾天蓝
```

### 地球(柔和化)
海洋 `#8FB4EA → #6B8FD6 → #4A67A8`,陆地 `#8FC7AE / #6FAE94`,云 alpha 0.25,光晕 primary alpha 0.12。

## 5. Proposed Changes(文件级)

### 5.1 [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt)
按§4 令牌重写双主题。字体 AppTypography 不变。

### 5.2 [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt)
替换为§4 的 6 组雾感渐变对,`hash % 6` 选取逻辑不变。

### 5.3 [EarthVisual.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/EarthVisual.kt)
按§4 柔和化配色替换(海洋/陆地/云/光晕/描边)。

### 5.4 [Common.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/Common.kt)
- `SectionTitle`:fontWeight Bold → SemiBold(收敛攻击性),样式 titleLarge 不变
- `TagChip`:结构不变,颜色随新主题自动柔化

### 5.5 [CityResultScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt)
- QuickInfoRow:圆角 16dp → 20dp,elevation 3dp → 4dp
- Header 结构不变(渐变/蒙层随 CityVisuals 自动柔化)

### 5.6 [SavedScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt)
- 列表卡:圆角 16dp → 20dp,elevation 2dp → 3dp

### 5.7 [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)
- Random 按钮:elevation 4dp(ButtonDefaults.elevatedButtonColors / cardElevation),其余不变

### 5.8 其他
- MainScreen / SplashScreen / RouteTimeline:颜色全部引用主题,零改动自动适配

## 6. 明确不做

- 不改功能/数据/导航;不动字体接入;不加动画;不引入图片
- 不再大改结构——本轮仅色彩、圆角、投影、字重的"柔和度"调优

## 7. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,20/20 单测保持全绿
2. 人工走查(用户验收):
   - 整体观感:浅色柔和,无刺眼色块,卡片为视觉主体
   - 连抽 5 城:封面均为雾感 pastel 渐变,白字清晰
   - 深色模式:柔和不灰暗
3. 验收通过后:commit + tag `v0.5.0`,继续 v0.6(Filters)
