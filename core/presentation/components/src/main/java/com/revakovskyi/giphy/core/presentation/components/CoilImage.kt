package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import com.revakovskyi.giphy.core.presentation.ui.theme.icons

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoilImage(
    imageLoader: ImageLoader,
    url: String,
    clickable: Boolean = true,
    onImageClick: () -> Unit = { },
    onLongPress: () -> Unit = { },
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .aspectRatio(1.0f)
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = { if (clickable) onImageClick() },
                onLongClick = {
                    if (clickable) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                }
            )
    ) {

        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = url,
            contentDescription = null,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            loading = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            error = {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaterialTheme.icons.imageWarning,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }
            },
        )

    }

}
