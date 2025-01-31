package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.components.GiphyButton
import com.revakovskyi.giphy.core.presentation.components.GiphyIconButton
import com.revakovskyi.giphy.gifs.domain.Constants.DEFAULT_AMOUNT_ON_PAGE
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsAction
import com.revakovskyi.giphy.gifs.presentation.gifs.GifsState
import com.revakovskyi.giphy.gifs.presentation.gifs.utils.PageDirection
import kotlinx.coroutines.launch

@Composable
fun PageButtonsSection(
    state: GifsState,
    gridState: LazyGridState,
    onAction: (action: GifsAction) -> Unit,
) {
    val scope = rememberCoroutineScope()

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
            if (state.currentPage > 1) {
                GiphyIconButton(
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    contentDescription = stringResource(R.string.previous),
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(1) }
                        onAction(GifsAction.ChangePage(PageDirection.Previous))
                    },
                )
            }

            if (state.currentPage > 2) {
                GiphyButton(
                    buttonText = stringResource(R.string.first),
                    buttonWidth = 100.dp,
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(1) }
                        onAction(GifsAction.ChangePage(PageDirection.First))
                    }
                )
            }

            if (state.gifs.size >= DEFAULT_AMOUNT_ON_PAGE) {
                GiphyIconButton(
                    contentDescription = stringResource(R.string.next),
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(1) }
                        onAction(GifsAction.ChangePage(PageDirection.Next))
                    },
                )
            }
        }

    }

}
