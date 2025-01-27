package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
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

}


@Preview
@Composable
private fun GifsScreenScreenPreview() {
    GiphyAppTheme {
        GifsScreenScreen(
            state = GifsState(),
            onAction = {}
        )
    }
}
