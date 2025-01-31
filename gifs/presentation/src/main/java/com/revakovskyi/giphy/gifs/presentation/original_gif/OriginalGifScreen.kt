package com.revakovskyi.giphy.gifs.presentation.original_gif

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.presentation.components.CoilImage
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.components.LoadingDialog
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent
import com.revakovskyi.giphy.core.presentation.ui.uitls.SnackBarController
import com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarEvent
import com.revakovskyi.giphy.gifs.presentation.R
import com.revakovskyi.giphy.gifs.presentation.original_gif.components.PagerButton
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun OriginalGifScreenRoot(
    viewModel: OriginalGifViewModel = koinViewModel(),
    gif: Gif,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SingleEvent(flow = viewModel.event) { event ->
        when (event) {
            is OriginalGifEvent.ShowNotification -> {
                scope.launch {
                    SnackBarController.sendEvent(
                        SnackBarEvent(message = event.message.asString(context))
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(OriginalGifAction.InitializeGif(gif))
    }

    OriginalGifScreenScreen(
        state = state,
        onAction = viewModel::onAction
    )

    AnimatedVisibility(
        label = "",
        visible = state.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LoadingDialog()
    }

}


@Composable
private fun OriginalGifScreenScreen(
    state: OriginalGifState,
    onAction: (action: OriginalGifAction) -> Unit,
) {

    val imageLoader: ImageLoader = koinInject()

    val pagerState = rememberPagerState { state.gifs.size }
    val gifSize by remember { mutableStateOf(450.dp) }


    LaunchedEffect(state.isLoading) {
        pagerState.animateScrollToPage(state.currentIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentIndex) {
            onAction(OriginalGifAction.UpdateCurrentIndex(pagerState.currentPage))
        }
    }

    GradientBackground(hasToolbar = false) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            androidx.compose.animation.AnimatedVisibility(
                label = "",
                visible = !state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(gifSize),
                    key = { state.gifs[it].id },
                    snapPosition = SnapPosition.Center,
                    modifier = Modifier.padding(16.dp),
                ) { page ->
                    CoilImage(
                        imageLoader = imageLoader,
                        url = state.gifs[page].urlOriginalImage,
                        clickable = false,
                    )
                }

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 64.dp)
            ) {

                PagerButton(
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                    visible = pagerState.currentPage != 0,
                    contentDescription = stringResource(R.string.previous),
                    onClick = { pagerState.animateScrollToPage(page = pagerState.currentPage - 1) }
                )

                PagerButton(
                    visible = pagerState.currentPage != state.gifs.lastIndex,
                    contentDescription = stringResource(R.string.next),
                    onClick = { pagerState.animateScrollToPage(page = pagerState.currentPage + 1) }
                )

            }

        }

    }

}


@Preview
@Composable
private fun OriginalGifScreenScreenPreview() {
    GiphyAppTheme {
        OriginalGifScreenScreen(
            state = OriginalGifState(),
            onAction = {}
        )
    }
}
