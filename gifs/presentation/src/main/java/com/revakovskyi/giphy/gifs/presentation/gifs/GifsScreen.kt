package com.revakovskyi.giphy.gifs.presentation.gifs

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.revakovskyi.giphy.core.presentation.components.GradientBackground
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme
import com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent
import org.koin.androidx.compose.koinViewModel

@Composable
fun GifsScreen(
    viewModel: GifsViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    com.revakovskyi.giphy.core.presentation.ui.uitls.SingleEvent(flow = viewModel.event) { uiText ->
        val text = uiText.asString(context)

        Log.d("TAG_Max", "GifsScreen.kt: error - $text")
        Log.d("TAG_Max", "")

    }

    com.revakovskyi.giphy.core.presentation.components.GradientBackground {

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
    com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme {
        GifsScreen()
    }
}
