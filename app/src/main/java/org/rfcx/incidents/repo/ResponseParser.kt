package org.rfcx.incidents.repo

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.Err
import org.rfcx.incidents.entity.ErrorResponse
import org.rfcx.incidents.entity.Ok
import org.rfcx.incidents.entity.Result
import org.rfcx.incidents.util.GsonProvider
import retrofit2.Response

class ResponseParserException(message: String) : Exception(message)
class ResponseUnauthenticatedException : Exception()

fun <T> responseParser(response: Response<T>?): Result<T, Exception> {

    if (response == null) {
        val exception = ResponseParserException("responseParser: response is null")
        return Err(exception)
    }

    if (response.isSuccessful) {
        val body = response.body()
        if (body != null) {
            return Ok(body)
        } else {
            return Err(ResponseParserException("responseParser: isSuccessful but body is null"))
        }
    }

    if (response.code() == 401) {
        return Err(ResponseUnauthenticatedException())
    }

    if (response.errorBody() == null) {
        return Err(ResponseParserException("error and missing error body"))
    }

    try {
        val error: ErrorResponse =
            GsonProvider.getInstance().gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
        return Err(ResponseParserException("error: ${error.message}"))
    } catch (e: Exception) {
        return Err(ResponseParserException("error: ${response.errorBody()}"))
    }
}

fun responseErrorHandler(
    error: Exception,
    callback: ApiCallback,
    context: Context,
    exceptionMessagePrefix: String = ""
) {
    when (error) {
        is ResponseUnauthenticatedException -> {
            FirebaseCrashlytics.getInstance().log(error.message.toString())
            callback.onFailed(TokenExpireException(context), null)
        }
        else -> {
            FirebaseCrashlytics.getInstance().log("$exceptionMessagePrefix ${error.message}")
            callback.onFailed(null, context.getString(R.string.error_common))
        }
    }
}
