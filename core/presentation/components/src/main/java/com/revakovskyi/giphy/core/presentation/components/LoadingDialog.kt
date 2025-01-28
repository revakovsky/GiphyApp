package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme

@Composable
fun LoadingDialog() {

    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(25.dp)
        ) {

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )

        }

    }

}


@Preview
@Composable
private fun PreviewSizesLoadingDialog() {
    GiphyAppTheme {
        LoadingDialog()
    }
}