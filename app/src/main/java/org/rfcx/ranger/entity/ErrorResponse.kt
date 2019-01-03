package org.rfcx.ranger.entity

data class ErrorResponse(
		val message: String, //invalid email or password
		val error: Error
)

data class Error(
		val status: Int //401
)