package com.revakovskyi.giphy.core.presentation.uitls.snack_bar_models

data class SnackBarAction(
    val name: String,
    val action: suspend () -> Unit,
)
