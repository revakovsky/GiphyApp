package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.components.GiphyButton
import com.revakovskyi.giphy.core.presentation.components.GiphyIconButton
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsAction
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsState
import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection

@Composable
fun PageButtonsSection(
    state: GifsState,
    onAction: (action: GifsAction) -> Unit,
) {

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 64.dp)
        ) {
            when (state.currentPage) {
                1 -> {
                    GiphyIconButton(
                        contentDescription = stringResource(R.string.next),
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.Next)) },
                    )
                }

                2 -> {
                    GiphyIconButton(
                        iconModifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                        contentDescription = stringResource(R.string.previous),
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.Previous)) },
                    )
                    GiphyIconButton(
                        contentDescription = stringResource(R.string.next),
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.Next)) },
                    )
                }

                else -> {
                    GiphyIconButton(
                        iconModifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                        contentDescription = stringResource(R.string.previous),
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.Previous)) },
                    )
                    GiphyButton(
                        buttonText = stringResource(R.string.first),
                        buttonWidth = 100.dp,
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.First)) }
                    )
                    GiphyIconButton(
                        contentDescription = stringResource(R.string.next),
                        onClick = { onAction(GifsAction.GetGifsForPage(PageDirection.Next)) },
                    )
                }
            }
        }

    }

}
