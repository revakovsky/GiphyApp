package com.revakovskyi.giphy.gifs.presentation.gifs.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.components.GiphyButton
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

            PagerButton(
                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                visible = state.currentPage > 1,
                contentDescription = stringResource(R.string.previous),
                onClick = {
                    onAction(GifsAction.ChangePage(PageDirection.Previous))
                    gridState.animateScrollToItem(1)
                }
            )

            Crossfade(
                label = "",
                targetState = state.currentPage > 2
            ) { show ->
                when (show) {
                    false -> Spacer(modifier = Modifier.width(100.dp))
                    true -> {
                        GiphyButton(
                            buttonText = stringResource(R.string.first),
                            buttonWidth = 100.dp,
                            onClick = {
                                scope.launch { gridState.animateScrollToItem(1) }
                                onAction(GifsAction.ChangePage(PageDirection.First))
                            }
                        )
                    }
                }
            }

            PagerButton(
                visible = state.gifs.size >= DEFAULT_AMOUNT_ON_PAGE,
                contentDescription = stringResource(R.string.next),
                onClick = {
                    onAction(GifsAction.ChangePage(PageDirection.Next))
                    gridState.animateScrollToItem(1)
                }
            )

        }

    }

}
