package com.revakovskyi.giphy.gifs.presentation.gifs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.theme.GiphyAppTheme

@Composable
fun GifsScreen() {

    GradientBackground {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = "Gifs Screen",
                style = MaterialTheme.typography.titleMedium,
            )

        }

    }

}


@PreviewScreenSizes
@Composable
private fun PreviewSizesGifsScreen() {
    GiphyAppTheme {
        GifsScreen()
    }
}
