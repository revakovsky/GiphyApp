package com.revakovskyi.giphy.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.revakovskyi.giphy.core.presentation.R

val Nunito = FontFamily(
    Font(R.font.nunito_regular, weight = FontWeight.Normal),
    Font(R.font.nunito_bold, weight = FontWeight.Bold),
    Font(R.font.nunito_extrabold, weight = FontWeight.ExtraBold),
)


val AppTypography = Typography(
    // Regular text
    bodyMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        color = GiphyWhite,
    ),

    // Title
    titleMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
        color = GiphyPinkLight,
    ),

    // Button text
    labelMedium = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center,
        color = GiphyWhite,
        shadow = Shadow(color = Color.Black, blurRadius = 15f),
    ),
)
