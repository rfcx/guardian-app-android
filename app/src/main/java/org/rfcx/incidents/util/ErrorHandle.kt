package org.rfcx.incidents.util

import android.content.Context
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.ErrorResponse
import org.rfcx.incidents.entity.ErrorResponse2
import retrofit2.HttpException
import java.io.IOException

class ApiException(val code: Int? = -1, message: String) : Exception(message)
class UnauthenticatedException : Exception()
class NetworkNotConnection : Exception()

fun HttpException?.getErrorFormApi(): Exception {

    if (this?.code() == 401) {
        return UnauthenticatedException()
    } else {
        if (this?.response()?.errorBody() == null) {
            return ApiException(this?.code(), "error and missing error body")
        }

        val url = this.response().raw().request.url.toString()
        val errString = this.response().errorBody()?.string()

        FirebaseCrashlytics.getInstance().log("API failed from $url,code ${this.code()} ===> response $errString")

        return try {
            val error: ErrorResponse = GsonProvider.getInstance().gson.fromJson(
                errString, ErrorResponse::class.java
            )
            ApiException(this.code(), error.message)
        } catch (e: Exception) {
            try {
                val error: ErrorResponse2 = GsonProvider.getInstance().gson.fromJson(
                    errString, ErrorResponse2::class.java
                )
                ApiException(this.code(), error.msg?.message ?: "error: $errString")
            } catch (e2: Exception) {
                ApiException(this.code(), "error: $errString")
            }
        }
    }
}

fun Throwable.getResultError(): Result.Error {
    this.printStackTrace()
    return when (this) {

        is IOException -> {
            Result.Error(NetworkNotConnection())
        }

        is HttpException -> {
            Result.Error(this.getErrorFormApi())
        }

        else -> {
            Result.Error(this)
        }
    }
}

/**
 * Handle error with event
 * For now show Toast error message
 * TODO custom to support other error display
 */
fun Context?.handleError(error: Throwable) {
    when (error) {
        is UnauthenticatedException -> {

            this?.let {
                logout()
            }
            Toast.makeText(this, this?.getString(R.string.login), Toast.LENGTH_SHORT).show()
        }

        is NetworkNotConnection -> {
            Toast.makeText(this, this?.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }

        is ApiException -> {
            Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        }

        else -> Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }
}
