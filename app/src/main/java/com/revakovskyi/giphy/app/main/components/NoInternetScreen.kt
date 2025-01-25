package com.revakovskyi.giphy.app.main.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.revakovskyi.giphy.core.presentation.theme.GiphyAppTheme

@Composable
fun NoInternetScreen() {

}


@PreviewScreenSizes
@Composable
private fun PreviewSizesNoInternetScreen() {
    GiphyAppTheme {
        NoInternetScreen()
    }
}