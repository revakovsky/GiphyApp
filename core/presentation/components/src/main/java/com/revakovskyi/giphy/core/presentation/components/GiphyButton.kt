package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme

@Composable
fun GiphyButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {

    Button(
        onClick = { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 5.dp,
            pressedElevation = 15.dp
        ),
        enabled = enabled,
        modifier = modifier
            .width(200.dp)
            .height(56.dp)
    ) {

        Text(
            text = buttonText,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            }
        )

    }

}


@Preview
@Composable
private fun PreviewSizesGiphyButton() {
    GiphyAppTheme {
        GiphyButton(
            buttonText = "Test text",
            enabled = true,
            onClick = {}
        )
    }
}