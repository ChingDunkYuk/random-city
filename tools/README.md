# 城市图片管线(tools/city_images.py)

Random City 城市代表图的**唯一**入库通道。所有图片离置内置在
`app/src/main/assets/city_images/`,App 离线秒显,不依赖运行时联网搜图。

## 图片标准(强制)

| 项 | 标准 |
|---|---|
| 文件名 | `{city_id}.webp`(与 cities.json 的 `id` 一致) |
| 尺寸 | 800 × 450(16:9,中心裁剪) |
| 格式 | WebP,质量 q80 起步,复杂场景自动降档(75/70/65/60) |
| 体积 | 10KB < 单张 ≤ 120KB |
| 内容 | 城市地标/天际线,横图,无水印,无人脸特写 |

## 环境

- Python 3.10+ 与 Pillow(已验证 12.3.0,需内置 WebP 支持)
- 全部命令在**仓库根目录**执行

## 命令

```powershell
# 校验:对照城市数据检查图齐全/无游离/格式/尺寸/体积(入库前必跑)
python tools/city_images.py validate

# 下载:只补缺失的城市图(新增城市后)
python tools/city_images.py download --only-missing

# 下载:重抓/补抓指定城市
python tools/city_images.py download --city tokyo

# 一次性迁移:存量 jpg 全量转 webp(v1.1.0 已执行,仅留档)
python tools/city_images.py convert-webp
```

## 新增一座城市的完整步骤

1. 在城市数据中加入该城(结构层 `assets/data/cities.json` + 文案层 `assets/data/cities_zh.json`)。
2. `python tools/city_images.py download --city {city_id}` 抓图。
3. **人工质检**:打开生成的 webp 逐张确认——主体正确、横图、无水印、无畸变;
   不合格就手动找一张 16:9 ≥800px 的图,用任意工具裁 800×450 存为同名 webp 覆盖。
4. `python tools/city_images.py validate` 通过。
5. `gradlew test` 通过(含内置图完整性单测),提交时图片与数据同 PR。

## 质检红线

- 下载脚本只保证"技术上合规"(尺寸/格式/体积),**画面内容必须人工过目**。
- 任何城市不得以"先入库后补图"的方式提交,`validate` 与单测会拦截。
- 旧 ps1 脚本(dl-city-images.ps1 / fix-city-images.ps1)已废弃删除,逻辑并入本管线。
