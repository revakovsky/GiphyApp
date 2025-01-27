package com.revakovskyi.giphy.core.presentation.ui.uitls

import com.revakovskyi.giphy.core.presentation.ui.R

interface QueryValidator {

    fun validate(input: String): UiText?

}


class QueryValidatorImpl : QueryValidator {

    override fun validate(input: String): UiText? {
        return when {
            input.startsWith(" ") ->
                UiText.StringResource(R.string.error_leading_space)

            input.any { !(it.isLetterOrDigit() || it.isWhitespace() || it.isPunctuation()) } ->
                UiText.StringResource(R.string.error_invalid_characters)

            input.any { it.isLetter() && (it !in 'a'..'z' && it !in 'A'..'Z') } ->
                UiText.StringResource(R.string.error_not_english)

            else -> null
        }
    }

    private fun Char.isPunctuation(): Boolean {
        return this in "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    }

}
