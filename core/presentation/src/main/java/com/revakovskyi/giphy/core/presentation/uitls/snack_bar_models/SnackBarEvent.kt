package com.revakovskyi.giphy.core.presentation.uitls.snack_bar_models

data class SnackBarEvent(
    val message: String,
    val action: SnackBarAction? = null,
)
