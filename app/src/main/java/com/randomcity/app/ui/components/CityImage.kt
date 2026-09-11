package com.randomcity.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.randomcity.app.RandomCityApp
import com.randomcity.app.domain.model.City

/**
 * 懒加载城市图片 URL:从 Application 容器取远程仓库,
 * produceState 按 city.id 触发,命中 LruCache/Coil 磁盘缓存时瞬时返回。
 */
@Composable
fun rememberCityImageUrl(city: City): String? {
    val context = LocalContext.current
    val urlState = produceState<String?>(initialValue = null, city.id) {
        val app = context.applicationContext as RandomCityApp
        value = app.container.cityRemoteRepository
            .fetchCityImageUrl(city.id, city.name, city.imageKeywords)
    }
    return urlState.value
}

/** 城市图片:URL 就绪后 paint 按 Crop 铺满 modifier 边界;未就绪时透明(调用方渐变兜底)。 */
@Composable
fun CityImage(city: City, modifier: Modifier = Modifier) {
    val url = rememberCityImageUrl(city)
    if (url != null) {
        Box(
            modifier = modifier.paint(
                painter = rememberAsyncImagePainter(model = url),
                contentScale = ContentScale.Crop
            )
        )
    }
}
