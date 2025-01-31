package com.revakovskyi.giphy.gifs.presentation.original_gif.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.components.GiphyIconButton
import kotlinx.coroutines.launch

@Composable
fun PagerButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    contentDescription: String,
    onClick: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Crossfade(
        label = "",
        targetState = visible
    ) { show ->
        when (show) {
            false -> Spacer(modifier = Modifier.width(48.dp))
            true -> {
                GiphyIconButton(
                    modifier = modifier,
                    contentDescription = contentDescription,
                    onClick = { scope.launch { onClick() } },
                )
            }
        }
    }

}
