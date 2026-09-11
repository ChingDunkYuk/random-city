<div align="center">
  <img src="docs/mascot.svg" width="120" alt="Random City Mascot" />

  # Random City

  **下一站,去哪呢?**

  把旅行交给命运 —— 每次点击,都是一次小小的出发。

  ![Version](https://img.shields.io/badge/version-1.0.0-2563EB)
  ![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
  ![Min SDK](https://img.shields.io/badge/minSdk-28-orange)
  ![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF)
  ![License](https://img.shields.io/badge/license-MIT-lightgrey)
</div>

---

## 这是什么

一个「随机城市发现」Android App。

打开 → **抽一座城市** → 花一分钟快速了解 → 心动就收藏 → 继续抽。
没有信息流、没有攻略墙,只有 200 座精心挑选的城市和一颗骰子。

## 功能

| 模块 | 内容 |
|---|---|
| 🎲 随机 | 200 城覆盖六大洲,洗牌滚动动画 + Reveal 动效 + 震动反馈 |
| 🏙 城市详情 | 代表图、一句话简介、天数/最佳月份/每日预算/交通、实时天气 |
| 🗺 旅行速览 | 必去景点、当地美食、住宿区域、当地交通、建议路线、旅行贴士 |
| 🧭 探索 | 大洲/风格/预算筛选、按心情探索、惊喜一下、精选城市 |
| ❤️ 记录 | 收藏(搜索/排序)、浏览历史(最近 100 条) |
| 🔗 直达 | 分享卡片、地图/机票/酒店/餐饮外链 |

**体验细节**

- 离线优先:城市数据 + 200 张代表图全部内置,断网可用(联网仅用于天气)
- 浅色卡片风 UI,完整深色模式
- Cinzel 碑刻体城市名、Q 版地球吉祥物、单手操作优化

## 技术栈

- **语言/UI**:Kotlin 2.0 · Jetpack Compose(Material 3)· Navigation Compose
- **数据**:Room(城市/收藏/历史)· DataStore(筛选/设置)· 手动 DI(无 Hilt)
- **网络**:OkHttp + Open-Meteo(天气,免 key)· Coil(图片)
- **质量**:JUnit 数据完整性测试(城市数据 ↔ 内置图一一对应)· R8 + 签名 Release

## 构建

```bash
./gradlew.bat assembleDebug      # 调试包
./gradlew.bat assembleRelease    # 发布包 → RandomCity-v{version}.apk
./gradlew.bat testDebugUnitTest  # 单元测试
```


## 项目结构

```
app/src/main/
├─ assets/
│  ├─ data/
│  │  ├─ cities.json         # 200 城结构数据(id/坐标/天数/预算/标签…)
│  │  └─ cities_zh.json      # 200 城中文文案(景点/美食/住宿/路线/贴士…)
│  └─ city_images/           # 200 张内置代表图(WebP 800×450,人工质检)
└─ java/com/randomcity/app/
   ├─ data/                  # Room / 远程仓库(天气) / Repository
   ├─ domain/                # 模型 / RandomEngine / RoutePlanner
   ├─ ui/                    # splash · home · city · saved · theme · components
   └─ navigation/

docs/plans/                  # v0.1 → v1.0 各阶段开发计划
tools/                       # 城市图片管线(city_images.py,Python+Pillow)
```

## 开发历程

从空目录到 v1.0,共 10 个里程碑,全部打 tag:

`v0.1 MVP` → `v0.3 详情增强` → `v0.5 建议路线` → `v0.6 筛选` → `v0.7 发现页` → `v0.8 收藏/历史` → `v0.9 联网与图片` → `v0.9.5 动效` → `v1.0 RC` → `v1.0 正式版`

各版本更新内容见 [CHANGELOG.md](CHANGELOG.md);阶段开发计划见 [docs/plans](docs/plans)。

## 素材许可

- 城市代表图:Wikipedia / Openverse(CC 授权)
- [Cinzel](https://fonts.google.com/specimen/Cinzel) 字体:Open Font License
- 代码:[MIT](LICENSE)
