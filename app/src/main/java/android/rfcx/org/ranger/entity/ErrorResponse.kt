package android.rfcx.org.ranger.entity

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */

data class ErrorResponse(
		val message: String, //invalid email or password
		val error: Error
)

data class Error(
		val status: Int //401
)