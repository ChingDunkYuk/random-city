# Hero 图片铺满修复 + 城市卡片真实图片

## Summary

用户反馈两点:
1. 详情页 Hero 图片**没有铺满**——截图显示图片以"居中小横幅"形式呈现,四周露出渐变背景。要求图片**铺满整个顶部区域**,且图片内容(城市代表图/景点图)保持正确。
2. **城市卡片**(首页"精选城市"横滑卡片、Saved 页收藏/历史行卡)目前是纯渐变色块,要求加载真实城市图片。

## Current State Analysis

### Hero 未铺满(详情页)

[CityResultScreen.kt](../../app/src/main/java/com/randomcity/app/ui/city/CityResultScreen.kt) `CityHeroHeader`(L344-442):

```kotlin
Box(fillMaxWidth().height(220.dp).background(渐变)) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()   // 理论上应铺满
        )
    }
    ...
}
```

- 真机截图现象 = 图片按**固有像素尺寸居中显示**(Bing 缩略图 800×450,在 xxhdpi 设备上约 267×150dp,正好约占屏宽 65%,与截图一致),即 `fillMaxSize + Crop` 未按预期生效。
- 不继续纠缠 `coil.compose.AsyncImage` 内部行为,改用更底层的 `Image + rememberAsyncImagePainter` 写法——painter 严格按 modifier 边界和 contentScale 绘制,**确定性铺满**。
- 图源逻辑(`CityRemoteRepository` Wikipedia→Bing 双源)已验证正确,**保持不变**。

### 城市卡片无图

- 首页精选卡片 `FeaturedCityCard`([HomeScreen.kt](../../app/src/main/java/com/randomcity/app/ui/home/HomeScreen.kt) L288-320):110×140dp 纯渐变色块 + 文字。
- Saved 页行卡 `CityRowCard`([SavedScreen.kt](../../app/src/main/java/com/randomcity/app/ui/saved/SavedScreen.kt) L274-320):52×52dp 渐变方块 + 文字。

### DI 结构

- [NavGraph.kt](../../app/src/main/java/com/randomcity/app/navigation/NavGraph.kt):`AppContainer` 传入 `MainScreen`,由其创建 `HomeViewModel`/`SavedViewModel`(L85-100)。
- 卡片是纯 composable,拿不到 container。改造最小的方式:共享 composable 内部从 `Application` 取 container,零 DI 签名改动;LazyRow/LazyColumn 只组合可见项,天然懒加载。

## Proposed Changes

### 1. 新建共享组件 `ui/components/CityImage.kt`

```kotlin
/** 懒加载城市图片 URL:从 Application 容器取远程仓库,
 *  LaunchedEffect 按 city.id 触发,命中 LruCache/Coil 磁盘缓存时瞬时返回。 */
@Composable
fun rememberCityImageUrl(city: City): String? {
    val context = LocalContext.current
    val urlState = produceState<String?>(initialValue = null, city.id) {
        val app = context.applicationContext as RandomCityApp
        value = app.container.cityRemoteRepository
            .fetchCityImageUrl(city.name, city.imageKeywords)
    }
    return urlState.value
}

/** 城市图片:URL 就绪后 Crop 铺满 modifier 边界;未就绪时透明(调用方用渐变兜底)。 */
@Composable
fun CityImage(city: City, modifier: Modifier = Modifier) {
    val url = rememberCityImageUrl(city)
    if (url != null) {
        Image(
            painter = rememberAsyncImagePainter(model = url),
            contentDescription = city.displayName,
            contentScale = ContentScale.Crop,
            modifier = modifier.clipToBounds()
        )
    }
}
```

要点:
- `produceState` 绑定 `city.id`,重组/分页复用安全;失败为 null → 调用方渐变兜底(与现有视觉一致)。
- 走 `CityRemoteRepository.imageCache`(LruCache 50)+ Coil 磁盘缓存,重复进入页面无二次请求。

### 2. Hero 铺满修复 — `ui/city/CityResultScreen.kt`

`CityHeroHeader` 中 `AsyncImage` 替换为底层写法:

```kotlin
if (imageUrl != null) {
    Image(
        painter = rememberAsyncImagePainter(model = imageUrl),
        contentDescription = city.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().clipToBounds()
    )
}
```

- `fillMaxSize().clipToBounds()` + Crop:图片**严格铺满** 220dp 高、全宽的 Hero Box,溢出部分裁剪,不再露出渐变(加载失败仍回落渐变)。
- 其余(顶部操作栏、底部蒙层、城市名/标签)不动。
- import 调整:移除 `coil.compose.AsyncImage`,新增 `androidx.compose.foundation.Image`、`coil.compose.rememberAsyncImagePainter`、`androidx.compose.ui.draw.clipToBounds`。

### 3. 首页精选卡片加图 — `ui/home/HomeScreen.kt`

`FeaturedCityCard`(L288-320)渐变 Box 内叠加:

```kotlin
Box(110×140dp, clip 圆角, 渐变背景) {
    CityImage(city = city, modifier = Modifier.fillMaxSize())   // 新增:铺满卡片
    Box(                                                        // 新增:底部蒙层
        fillMaxWidth().height(60.dp).align(BottomCenter)
            .background(垂直渐变 Transparent → Black 0.45)
    )
    Column(底部文字, 不变)
}
```

- 图片未就绪/失败时显示原渐变,视觉无缝退化。

### 4. Saved 行卡缩略图 — `ui/saved/SavedScreen.kt`

`CityRowCard`(L294-300)52dp 渐变方块内叠加 `CityImage`:

```kotlin
Box(size(52.dp).clip(RoundedCornerShape(12.dp)).background(渐变)) {
    CityImage(city = city, modifier = Modifier.fillMaxSize())
}
```

### 5. 编译出 APK

`.\gradlew.bat assembleDebug`,产物 `app/build/outputs/apk/debug/app-debug.apk`。

## Assumptions & Decisions

1. **"城市卡片页" = 首页精选卡片 + Saved 页行卡**,两处都改为真实图片(同一共享组件,成本低)。
2. **图源逻辑不变**(Wikipedia→Bing 双源,用户已确认"图片是对的")。
3. Hero 高度保持 220dp 不变,用户诉求是"铺满"而非"加高"。
4. 卡片图片采用**组件级懒加载**(可见才请求),不改 ViewModel/DI 签名;首页精选约 10 张、Saved 列表滚动加载,配合双层缓存流量可控。
5. 不新增占位图资源:加载中/失败一律回落到现有渐变,保持视觉一致。

## Verification

1. `.\gradlew.bat assembleDebug` 编译通过。
2. 真机安装后检查:
   - 详情页 Hero:图片**全宽全高铺满**,无渐变露出(断网时回落渐变为预期)。
   - 多个城市页(含之前 load 不出的)图片正确且铺满。
   - 首页"精选城市"卡片显示真实图片,底部文字清晰可读。
   - Saved 页收藏/历史行卡左侧显示圆形方角缩略图。
   - 二次进入同一城市页图片瞬时显示(缓存生效)。
