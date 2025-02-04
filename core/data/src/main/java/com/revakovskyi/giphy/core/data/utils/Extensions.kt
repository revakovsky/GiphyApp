package com.revakovskyi.giphy.core.data.utils

import android.database.sqlite.SQLiteFullException
import com.revakovskyi.giphy.core.domain.util.DataError
import com.revakovskyi.giphy.core.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.net.UnknownHostException

suspend inline fun <reified T> safeCall(crossinline execute: suspend () -> Response<T>): Result<T, DataError.Network> {
    return withContext(Dispatchers.IO) {
        val response = try {
            execute()
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return@withContext Result.Error(DataError.Network.NO_INTERNET)
        } catch (e: SerializationException) {
            e.printStackTrace()
            return@withContext Result.Error(DataError.Network.SERIALIZATION)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            return@withContext Result.Error(DataError.Network.UNKNOWN)
        }

        responseToResult(response)
    }
}


inline fun <reified T> responseToResult(response: Response<T>): Result<T, DataError.Network> {
    return when (response.code()) {
        in 200..299 -> {
            response.body()?.let { Result.Success(it) } ?: Result.Error(DataError.Network.SERIALIZATION)
        }
        400 -> Result.Error(DataError.Network.BAD_REQUEST)
        401 -> Result.Error(DataError.Network.UNAUTHORIZED)
        403 -> Result.Error(DataError.Network.FORBIDDEN)
        404 -> Result.Error(DataError.Network.NOT_FOUND)
        414 -> Result.Error(DataError.Network.URI_TOO_LONG)
        429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
        else -> Result.Error(DataError.Network.UNKNOWN)
    }
}

suspend inline fun <T> safeDbCall(crossinline action: suspend () -> T): Result<T, DataError.Local> {
    return withContext(Dispatchers.IO) {
        try {
            Result.Success(action())
        } catch (e: SQLiteFullException) {
            e.printStackTrace()
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
