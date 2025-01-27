package com.revakovskyi.giphy.core.domain.util

sealed interface DataError : Error {

    enum class Network : DataError {
        UNAUTHORIZED,
        FORBIDDEN,
        BAD_REQUEST,
        NOT_FOUND,
        TOO_MANY_REQUESTS,
        URI_TOO_LONG,
        REQUEST_TIMEOUT,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        NO_INTERNET,
        SERIALIZATION,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN,
    }

}