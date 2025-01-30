package com.revakovskyi.giphy.gifs.presentation.original_gif

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.core.domain.gifs.Gif
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.components.LoadingDialog
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent
import com.revakovskyi.giphy.core.presentation.ui.uitls.SnackBarController
import com.revakovskyi.giphy.core.presentation.ui.uitls.snack_bar_models.SnackBarEvent
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OriginalGifScreenRoot(
    viewModel: OriginalGifViewModel = koinViewModel(),
    gif: Gif,
) {
    val state = viewModel.originalGifState.collectAsStateWithLifecycle().value

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

        Log.d("TAG_Max", "OriginalGifScreen.kt: gotten gif = $gif")
        Log.d("TAG_Max", "")

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

    GradientBackground(hasToolbar = false) {

        Row(modifier = Modifier.fillMaxSize()) {

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
