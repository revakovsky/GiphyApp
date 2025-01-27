package com.revakovskyi.giphy.core.presentation.ui.uitls

import com.revakovskyi.giphy.core.presentation.ui.R

interface QueryValidator {

    fun appropriateLanguage(input: String): UiText?
    fun validate(input: String): UiText?

}


class QueryValidatorImpl : QueryValidator {

    override fun appropriateLanguage(input: String): UiText? {
        return if (
            input.all {
                it.isLetter() &&
                        it in 'a'..'z' || it in 'A'..'Z' ||
                        it.isWhitespace()
            }
        ) {
            null
        } else {
            UiText.StringResource(R.string.error_not_english)
        }
    }

    override fun validate(input: String): UiText? {
        return when {
            input.isBlank() -> UiText.StringResource(R.string.error_empty_input)
            input.startsWith(" ") -> UiText.StringResource(R.string.error_leading_space)
            input.any { !it.isLetterOrDigit() && !it.isWhitespace() } -> UiText.StringResource(R.string.error_invalid_characters)
            else -> null
        }
    }

}
