package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.core.presentation.components.GiphyTextField
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.rememberImeState
import com.revakovskyi.giphy.gifs.presentation.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun GifsScreenRoot(
    viewModel: GifsViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    GifsScreenScreen(
        state = state,
        onAction = viewModel::onAction
    )

}


@Composable
private fun GifsScreenScreen(
    state: GifsState,
    onAction: (GifsAction) -> Unit,
) {
    val imeStateOpen by rememberImeState()
    val localFocusManager = LocalFocusManager.current

    LaunchedEffect(imeStateOpen) {
        if (!imeStateOpen) localFocusManager.clearFocus()
    }

    GradientBackground(hasToolbar = false) {

        GiphyTextField(
            text = state.searchingQuery,
            title = stringResource(R.string.input_a_searching_query),
            hint = stringResource(R.string.your_query),
            error = state.errorMessage?.asString(),
            onTextChange = { input -> onAction(GifsAction.QueryEntered(input)) },
            onClearClick = { onAction(GifsAction.ClearQuery) },
            onDoneClick = { onAction(GifsAction.Search) }
        )

    }

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
