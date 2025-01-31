package com.revakovskyi.giphy.core.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.revakovskyi.giphy.core.presentation.ui.theme.GiphyAppTheme

@Composable
fun GiphyIconButton(
    modifier: Modifier = Modifier,
    contentDescription: String,
    icon: ImageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
    buttonsSize: Dp = 48.dp,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit,
) {

    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
        modifier = modifier.size(buttonsSize)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }

}

@Preview
@Composable
private fun PreviewSizesGiphyIconButton() {
    GiphyAppTheme {
        GiphyIconButton(
            contentDescription = "Test",
            onClick = {}
        )
    }
}