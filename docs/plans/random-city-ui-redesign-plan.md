# Random City UI 重构计划(深色高级感)

## 1. Summary

对 MVP 全部页面做视觉系统重构,替换掉当前"默认 M3 + 随机彩色渐变 + emoji"的廉价感方案。
方向:**深色高级感**(深灰蓝底、低饱和蓝点缀、1px 细边框卡片、Material Symbols 线性图标、去 emoji);
城市封面:**统一品牌蓝系渐变**(每城仅明度/色相微调)。
不改任何功能逻辑与数据层,重构完成后验收 v0.5.0。

## 2. Current State Analysis

现状问题(用户两次反馈"丑"):
- [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt):近似默认 M3 配色,无个性
- [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt):10 组随机彩色渐变(青/靛/绿混杂),封面观感廉价
- TagChip 使用 `secondaryContainer` 实色填充 + emoji;按钮文案带 🎲 emoji
- 卡片用 `surfaceVariant` 灰底,无边框,层次靠色块堆砌
- EarthVisual 配色偏卡通(亮蓝鲜绿)

用户偏好(记忆):拒绝粉色/暖色/深空黑/星场动效;期望飞书工单式干净界面、Material Symbols 图标风格。

## 3. 设计决策(已与用户确认)

| 决策 | 方案 |
|---|---|
| 整体方向 | 深色高级感为主设计;浅色同步重构保持干净(跟随系统,后续版本加设置项) |
| 城市封面 | 统一品牌蓝系渐变,hue 216±8、低饱和,每城仅微调明度/色相 |
| 图标 | Material Symbols 线性(Icons.Outlined),骰子用 `Icons.Outlined.Casino`(material-icons-extended 已在依赖中) |
| emoji | UI 全面去除(按钮、Tag、Splash 均不用) |
| 卡片语言 | 深色 surface + 1px outline 细边框,12–14dp 圆角,不用色块堆砌 |

## 4. 设计令牌(锁定)

### 深色(旗舰)
```text
background      #0F141B   深灰蓝(非纯黑)
surface         #161D27   卡片底
surfaceVariant  #1C242F   次级底
outline         #2A3442   1px 边框
primary         #7C9FE8   低饱和蓝
onSurface       #E8EBF0
onSurfaceVariant#9AA3B2
```

### 浅色(干净版,同步交付)
```text
background      #F5F6F7   飞书式浅灰
surface         #FFFFFF
outline         #E2E5EA
primary         #3370FF   飞书蓝
onSurface       #1F2329
onSurfaceVariant#646A73
```

### 封面渐变(统一品牌系)
`Color.hsl(hue = 216 + (hash % 17 - 8), sat = 0.42f, lightness 0.30f → 0.16f)`
即全部城市落在 208–224 蓝区间,仅明度/色相微调,白字恒可读。

## 5. Proposed Changes(文件级)

### 5.1 [Theme.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/theme/Theme.kt)
按§4 令牌重写 LightColors / DarkColors;不动主题切换逻辑(仍跟随系统)。

### 5.2 [CityVisuals.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/util/CityVisuals.kt)
删除 10 组随机调色板,改为 HSL 程序化生成:由 cityId hash 派生 hue 偏移(±8)与明度档(0.30/0.16),输出统一蓝系 `Pair<Color, Color>`。签名不变,调用方零改动。

### 5.3 [Common.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/Common.kt)
- `TagChip`:改 1px outline 描边样式(透明底、labelMedium、圆角 50),移除 emoji 参数;新增 `accent` 参数(贴士分类 chip 用 primary 描边)
- `SectionTitle`:加 3dp×14dp primary 圆头竖条 + 文本,提升识别度
- 删除未使用的 `QuickInfoCard`(QuickInfoRow 在屏幕文件内)

### 5.4 [EarthVisual.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/EarthVisual.kt)
配色沉下来:海洋 `#16345C → #0C1B33`,陆地 `#2F5D4B / #3D7263`,云 alpha 0.15,光晕 alpha 0.10。深浅主题下均为深蓝地球。

### 5.5 [HomeScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt)
- Random 按钮:去 🎲,`Icon(Icons.Outlined.Casino)` + "Random" 文案,高 60dp
- 副文案保留;地球保留(配色由 5.4 收敛);顶部留白与层级微调

### 5.6 [CityResultScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt)
- Header:统一品牌渐变(自动来自新 CityVisuals),高 200dp;Header 上 TagChip 用白 0.5 alpha 描边变体
- QuickInfoRow:surface + 1px outline,四列间加 1px 细分隔线
- 住宿区行:行间加 `HorizontalDivider`(0.5dp, outline 40% alpha)取代点号
- Random Again:去 🎲,Casino 图标 + 文案
- 其余结构/间距不变(上一轮紧凑化成果保留)

### 5.7 [SavedScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt)
- 列表卡:surface + 1px outline,圆角 12dp
- 缩略图:统一蓝系渐变(新 CityVisuals),圆角 10dp
- 删除图标改 `Icons.Outlined.DeleteOutline`,空态去 emoji

### 5.8 [MainScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/main/MainScreen.kt)
- NavigationBar:`containerColor = surface`,tonalElevation 0,顶部加 0.5dp 分隔线
- 图标:选中 `Icons.Filled.Explore / Favorite`,未选中 `Icons.Outlined.Explore / FavoriteBorder`

### 5.9 [SplashScreen.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/splash/SplashScreen.kt)
去 🎲 emoji,改 `Icons.Outlined.Casino`(64dp, primary),字标与进度圈配色随新主题。

### 5.10 [RouteTimeline.kt](file:///d:/Random%20city/app/src/main/java/com/randomcity/app/ui/components/RouteTimeline.kt)
不改:颜色全部引用主题(primary / outline),自动适配。

## 6. 明确不做

- 不加动画/辉光/扫描线等动效(用户明确拒绝)
- 不改功能逻辑、数据层、导航结构
- 不加主题设置入口(留待后续 Settings 版本)
- 不引入图片加载(v0.9 阶段)

## 7. Verification

1. `gradlew.bat assembleDebug testDebugUnitTest`:编译通过,20/20 单测保持全绿(无逻辑变更)
2. 人工走查(用户验收):
   - 深色模式下:背景非纯黑、卡片边框克制、蓝点缀统一、无 emoji
   - 浅色模式下:飞书式白卡灰底、飞书蓝主色
   - 任意连抽 5 城:封面均为蓝系渐变、白字可读
   - 首页地球为深蓝色调;Random/Random Again 为图标+文字按钮
3. 验收通过后:commit + tag `v0.5.0`,继续 v0.6(Filters)
