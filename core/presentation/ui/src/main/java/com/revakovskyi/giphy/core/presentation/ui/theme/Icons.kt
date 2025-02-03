package com.revakovskyi.giphy.core.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.revakovskyi.giphy.core.presentation.ui.R

class Icons {

    val logo: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.logo)

    val wiFi: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.wi_fi_off)

    val clear: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.clear)

    val inputLight: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.input_light)

    val inputBold: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.input_bold)

    val imageWarning: ImageVector
        @Composable get() = ImageVector.vectorResource(id = R.drawable.image_warning)

}

val LocalIcons = compositionLocalOf { Icons() }

val MaterialTheme.icons: Icons
    @Composable
    @ReadOnlyComposable
    get() = LocalIcons.current