package com.revakovskyi.giphy.core.presentation.ui.uitls

import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.presentation.ui.R

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Local.DISK_FULL -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_disk_full
        )

        DataError.Local.UNKNOWN -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_db_unknown
        )

        DataError.Network.BAD_REQUEST -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_bad_request
        )

        DataError.Network.UNAUTHORIZED -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_unauthorized
        )

        DataError.Network.FORBIDDEN -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_forbidden
        )

        DataError.Network.NOT_FOUND -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_not_found
        )

        DataError.Network.TOO_MANY_REQUESTS -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_too_many_requests
        )

        DataError.Network.URI_TOO_LONG -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_uri_too_long
        )

        DataError.Network.REQUEST_TIMEOUT -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_request_timeout
        )

        DataError.Network.PAYLOAD_TOO_LARGE -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_payload_too_large
        )

        DataError.Network.SERVER_ERROR -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_server_error
        )

        DataError.Network.NO_INTERNET -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_no_internet
        )

        DataError.Network.SERIALIZATION -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_serialization
        )

        DataError.Network.UNKNOWN -> com.revakovskyi.giphy.core.presentation.ui.uitls.UiText.StringResource(
            R.string.error_unknown
        )
    }
}
