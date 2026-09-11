# Random City

> 下一站,去哪呢?

一个「随机城市发现」Android App:打开 → 抽一座城市 → 快速了解 → 收藏 → 继续抽。
灵感来自"把旅行交给命运"——每次点击都是一次小小的出发。

## 功能

- **随机抽取**:103 座城市覆盖六大洲,洗牌滚动动画 + 结果页 Reveal 动效 + 震动反馈
- **城市详情**:内置代表图(离线秒显)、一句话简介、天数/最佳月份/每日预算/交通便利度、当前天气(联网)、必去景点、当地美食、住宿区域、当地交通、建议路线、旅行贴士
- **探索方式**:大洲/旅行风格/预算多维筛选、按心情探索(美食/海岛/摄影/历史/自然)、惊喜一下(完全随机)、精选城市
- **个人记录**:收藏(搜索/排序)、浏览历史(最近 100 条)
- **出行直达**:分享城市卡片、地图/机票/酒店/餐饮外链
- **体验细节**:浅色卡片风 UI、深色模式、Cinzel 碑刻城市名、Q 版地球吉祥物、单手操作优化
- **离线优先**:城市数据(Room)+ 代表图(assets)全内置,断网可用;联网仅用于天气

## 技术栈

- Kotlin 2.0 + Jetpack Compose(Material 3)+ Navigation Compose
- Room(城市/收藏/历史)+ DataStore(筛选/设置)
- Coil(图片)+ OkHttp(天气 Open-Meteo,免 key)
- MVVM + Repository + 手动 DI(无 Hilt)
- JUnit 数据完整性测试(城市数据/内置图一一对应)

## 项目结构

```
app/src/main/
├─ assets/cities.json + city_images/   # 103 城数据 + 内置代表图(800x450)
├─ java/com/randomcity/app/
│  ├─ data/(local|remote|repository|model)
│  ├─ domain/(model|RandomEngine|RoutePlanner)
│  ├─ ui/(splash|home|city|saved|main|theme|components)
│  └─ navigation/NavGraph.kt
docs/plans/                            # 各阶段开发计划(v0.1 → v1.0)
tools/                                 # 城市图批量下载/修复脚本(PowerShell)
```

## 素材许可

- 城市代表图:Wikipedia / Openverse(CC 授权)
- Cinzel 字体:Open Font License
- 代码:MIT
