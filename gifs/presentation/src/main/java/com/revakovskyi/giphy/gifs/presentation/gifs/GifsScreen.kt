package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.components.LoadingDialog
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent
import com.revakovskyi.giphy.core.presentation.ui.uitls.SnackBarController
import com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarEvent
import com.revakovskyi.giphy.gifs.presentation.gifs.components.GifsSection
import com.revakovskyi.giphy.gifs.presentation.gifs.components.PageButtonsSection
import com.revakovskyi.giphy.gifs.presentation.gifs.components.SearchingFieldWithButtonsSection
import com.revakovskyi.giphy.gifs.presentation.gifs.components.UserHint
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun GifsScreenRoot(
    viewModel: GifsViewModel = koinViewModel(),
    openOriginalGif: (gifId: String) -> Unit,
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler {
        (context as ComponentActivity).finish()
    }

    SingleEvent(flow = viewModel.event) { event ->
        when (event) {
            is GifsEvent.OpenOriginalGif -> openOriginalGif(event.gifId)
            is GifsEvent.ShowNotification -> {
                scope.launch {
                    SnackBarController.sendEvent(
                        SnackBarEvent(message = event.message.asString(context))
                    )
                }
            }
        }
    }

    GifsScreenScreen(
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
private fun GifsScreenScreen(
    state: GifsState,
    onAction: (GifsAction) -> Unit,
) {
    val gridState = rememberLazyGridState()

    GradientBackground(hasToolbar = false) {

        SearchingFieldWithButtonsSection(
            state = state,
            onAction = onAction
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {

            Crossfade(
                label = "",
                targetState = state.gifs.isEmpty(),
            ) { noGifs ->
                when (noGifs) {
                    true -> UserHint()
                    false -> {
                        GifsSection(
                            state = state,
                            gridState = gridState,
                            onAction = onAction
                        )
                    }
                }
            }

        }

    }

    PageButtonsSection(
        state = state,
        gridState = gridState,
        onAction = onAction
    )

}


@PreviewScreenSizes
@Composable
private fun GifsScreenScreenPreview() {
    GiphyAppTheme {
        GifsScreenScreen(
            state = GifsState(),
            onAction = {}
        )
    }
}
