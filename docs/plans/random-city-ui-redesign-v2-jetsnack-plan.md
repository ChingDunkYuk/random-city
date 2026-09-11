# Random City UI 重构 v2 计划(Jetsnack 明快卡片风 + Space Grotesk)

## 1. Summary

上一版深色方案被否(太闷、描边 chip 太弱、封面渐变浑浊)。
本次以 GitHub 官方高星项目 [Jetsnack (android/compose-samples)](https://github.com/android/compose-samples/tree/main/Jetsnack) 为设计参考重做视觉系统:

- **大圆角卡片**(16dp + 轻投影,取代 1px 描边)
- **蓝→青冷色系明快渐变**封面(取代浑浊统一深渐变)
- **药丸按钮/标签**(tonal 软填充,无描边)
- **Space Grotesk 西文字体**(Google Fonts downloadable,离线自动回退系统字体)
- 宽松留白 + 明确字级层级

不改功能逻辑与数据层;完成后验收 v0.5.0。

## 2. Current State Analysis

现存问题(对应"更丑了"):
- [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt):深灰蓝整体过闷,primary 低饱和显旧
- [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt):统一深渐变(lightness 0.30→0.16)浑浊无光
- [Common.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/Common.kt):描边 chip 视觉弱,竖条标题生硬
- 无字体层级:城市名/正文同为系统默认字重,无记忆点

参考项目设计要点(Jetsnack):白/浅底 + 大圆角白卡 + 轻投影;品牌色高饱和蓝紫;卡片封面用明快双色渐变;按钮药丸形;标题粗黑大字、正文灰;chips 软色填充。

## 3. 设计决策(已与用户确认)

| 决策 | 方案 |
|---|---|
| 参考项目 | Jetsnack(明快卡片风) |
| 西文字体 | Space Grotesk,downloadable font(`ui-text-google-fonts`,Compose BOM 2024.12.01 已含 1.7.6) |
| 色彩基调 | 冷色系高饱和蓝为主、蓝→青渐变,拒绝暖色/粉 |
| 深色模式 | 保留,深色版同步"提亮"(深靛蓝底 + 亮蓝 primary,不再灰暗) |

## 4. 设计令牌(锁定)

### 浅色(主)
```text
background      #FBFCFD
surface         #FFFFFF
primary         #4C6FFF   明快蓝
primaryContainer#E3EAFF
onSurface       #1A1D26
onSurfaceVariant#5A6070
outline         #E8EAEF
卡片            16dp 圆角 + elevation 3dp
按钮/chip       药丸(50% 圆角)
```

### 深色(提亮版)
```text
background      #10141D   深靛蓝灰(非黑)
surface         #1A2029
primary         #8AABFF
primaryContainer#2A3A6B
onSurface       #EDEFF5
onSurfaceVariant#9BA3B4
outline         #2C3542
```

### 封面渐变(CityVisuals v3,6 组明快冷色系,按 cityId hash 选取)
```text
#5B8DEF → #3A5FCD   经典蓝
#38B2C8 → #2E7FD9   青 → 蓝
#4E63E7 → #2F3FA8   靛蓝
#2FA8B8 → #2563C4   蓝绿 → 蓝
#6A7FE8 → #3B4BB8   柔靛
#3E9BE0 → #2B5FC7   天蓝 → 蓝
```
Header 文字区叠加 `Black 20%` 蒙层保证白字可读。

### 字体(Type.kt)
- `FontFamily`:Space Grotesk Light/Regular/Medium/SemiBold/Bold(downloadable)
- `provider`:`GoogleFont.Provider(authority "com.google.android.gms.fonts", package "com.google.android.gms", certs R.array.com_google_android_gms_fonts_certs)`
- 应用于 display/headline/title/label 系列;body 保持系统字体(中文混排自然)
- 城市英文名:`displayLarge Space Grotesk Bold 40sp, letterSpacing 0.5sp`

## 5. Proposed Changes(文件级)

### 5.1 字体接入
- [build.gradle.kts](file:///d:/Random%20city/app/build.gradle.kts):加 `implementation("androidx.compose.ui:ui-text-google-fonts")`(版本由 BOM 管理)
- 新增 `app/src/main/res/values/font_certs.xml`:Google Play services 字体证书数组(官方标准内容)
- 新增 `ui/theme/Type.kt`:provider、SpaceGrotesk FontFamily、`AppTypography`

### 5.2 [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt)
按§4 令牌重写双主题;`MaterialTheme(colorScheme, typography = AppTypography)`。

### 5.3 [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt)
删除 HSL 深渐变生成,改为 6 组§4 明快渐变对,`hash % 6` 选取。签名不变。

### 5.4 [Common.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/Common.kt)
- `TagChip`:改 tonal 软填充药丸(primaryContainer 底 + primary 字,无描边);`accent` 参数移除;Header 用白色 0.2 alpha 填充变体(contentColor/borderColor 参数删除,改为 `container: Color?` 重载)
- `SectionTitle`:去竖条,改 titleLarge Bold 纯排版标题

### 5.5 [EarthVisual.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/EarthVisual.kt)
海洋提亮:`#3E9BE0 → #2B5FC7 → #1E3E8C`,陆地 `#3FA37E / #2E7D64`,云 alpha 0.2,光晕 alpha 0.15。

### 5.6 [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)
- Random 按钮:药丸(50% 圆角)高 60dp,Casino 图标保留
- 标题用 Space Grotesk 大字;副文案 onSurfaceVariant

### 5.7 [CityResultScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt)
- Header:220dp 明快渐变 + 底部 20% 黑蒙层;城市名 Space Grotesk displayLarge;标签 chip 白色 0.2 alpha 填充
- QuickInfoRow:16dp 圆角白卡 + 3dp 投影,去列分隔线(改等宽间距),数字用 Space Grotesk
- 住宿区:去分隔线,改 8dp 间距 + 名称粗体/说明灰字的呼吸感列表
- chips 全部 tonal;Random Again 药丸按钮
- RouteTimeline 自动随主题

### 5.8 [SavedScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt)
- 列表卡:16dp 圆角 + elevation 2dp,去描边
- 缩略图:明快渐变,圆角 12dp;标题 Space Grotesk

### 5.9 [MainScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/main/MainScreen.kt)
- NavigationBar:surface 底 + tonalElevation 3dp,图标保持 Explore/Favorite 线性与填充切换

### 5.10 [SplashScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/splash/SplashScreen.kt)
- 字标应用 Space Grotesk displayLarge;图标/进度随新主题色

## 6. 明确不做

- 不改功能/数据/导航;不加动画辉光;不引入图片加载;不加设置页
- 字体走 downloadable font,不内置 APK(离线回退系统字体,不阻塞启动)

## 7. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,20/20 单测保持全绿
2. 人工走查(用户验收):
   - 首页:大字标题 + 明快地球 + 药丸 Random 按钮
   - 连抽 5 城:封面均为明快冷色渐变、城市名 Space Grotesk 大字、白字清晰
   - QuickInfo/收藏卡:大圆角 + 轻投影,无描边感
   - 深色模式:深靛蓝不灰暗,primary 亮蓝
   - 断网启动:字体回退系统字体,UI 不崩
3. 验收通过后:commit + tag `v0.5.0`,继续 v0.6(Filters)
