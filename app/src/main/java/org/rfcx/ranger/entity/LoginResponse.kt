package org.rfcx.ranger.entity

/**
 * Created by Jingjoeh on 10/2/2017 AD.
 */




data class LoginResponse(
		val guid: String, //2ada55fb-433f-4074-a3c8-aa3f146fc10e
		val type: String, //user
		val username: Any, //null
		val firstname: String, //Komkrit
		val lastname: String, //Banglad
		val email: String, //jingjoeh@gmail.com
		val is_email_validated: Boolean, //false
		val last_login_at: String, //2017-10-02T18:02:53.962Z
		val tokens: List<Token>
)

data class Token(
		val token: String, //258u6fb1i13fh8wc4813amo2qcb621qxjui34n1x
		val token_expires_at: String //2017-10-03T18:02:53.963Z
)

