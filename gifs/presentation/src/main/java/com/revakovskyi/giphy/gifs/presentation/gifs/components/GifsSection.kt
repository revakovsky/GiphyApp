package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.revakovskyi.giphy.core.presentation.components.CoilImage
import com.revakovskyi.giphy.core.presentation.components.GiphyIconButton
import com.revakovskyi.giphy.core.presentation.ui.theme.icons
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsAction
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsState
import org.koin.compose.koinInject

@Composable
fun GifsSection(
    state: GifsState,
    gridState: LazyGridState,
    onAction: (action: GifsAction) -> Unit,
) {
    val imageLoader: ImageLoader = koinInject()

    var selectedGifId by remember { mutableStateOf<String?>(null) }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(bottom = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(horizontal = 16.dp),
        content = {
            items(
                items = state.gifs,
                key = { it.id }
            ) { gif ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {

                    CoilImage(
                        imageLoader = imageLoader,
                        url = gif.urlSmallImage,
                        onImageClick = { onAction(GifsAction.OpenOriginalGif(gifId = gif.id)) },
                        onLongPress = { selectedGifId = gif.id }
                    )

                    if (selectedGifId == gif.id) {
                        ContextMenuButtons(
                            onCancelClick = { selectedGifId = null },
                            onDeleteClick = {
                                onAction(GifsAction.DeleteGif(gif.id))
                                selectedGifId = null
                            },
                        )
                    }

                }

            }
        }
    )

}


@Composable
private fun BoxScope.ContextMenuButtons(
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 12.dp)
    ) {
        GiphyIconButton(
            contentDescription = stringResource(id = R.string.delete_gif),
            icon = Icons.Default.Delete,
            buttonsSize = 32.dp,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDeleteClick()
            }
        )

        GiphyIconButton(
            contentDescription = stringResource(id = R.string.cancel),
            icon = MaterialTheme.icons.clear,
            buttonsSize = 32.dp,
            onClick = onCancelClick
        )
    }

}
