package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.rfcx.ranger.R
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import io.jsonwebtoken.Jwts
import kotlinx.android.synthetic.main.activity_login.*
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.Result


class LoginActivity : AppCompatActivity() {
	val metaDataKey = "https://rfcx.org/app_metadata"
	
	companion object {
		fun startActivity(context: Context) {
			context.startActivity(Intent(context, LoginActivity::class.java))
		}
	}
	
	private val auth0 by lazy {
		val auth0 = Auth0(this)
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)
		
		if (isLogin()) {
			MessageListActivity.startActivity(this@LoginActivity)
			finish()
		} else {
			loginEmailEditText.visibility = View.VISIBLE
			loginPasswordEditText.visibility = View.VISIBLE
			loginButton.visibility = View.VISIBLE
		}
		
		loginButton.setOnClickListener {
			val email = loginEmailEditText.text.toString()
			val password = loginPasswordEditText.text.toString()
			if (validateInput(email, password)) {
				doLogin(email, password)
			}
		}
	}
	
	private fun validateInput(email: String?, password: String?): Boolean {
		if (email.isNullOrEmpty()) {
			loginEmailEditText.error = getString(R.string.pls_fill_email)
			return false
		} else if (password.isNullOrEmpty()) {
			loginPasswordEditText.error = getString(R.string.pls_fill_password)
			return false
		}
		return true
	}
	
	private fun doLogin(email: String, password: String) {
		loginProgress.visibility = View.VISIBLE
		loginErrorTextView.visibility = View.INVISIBLE
		loginButton.isEnabled = false
		loginEmailEditText.isEnabled = false
		loginPasswordEditText.isEnabled = false
		
		authentication
				.login(email, password, "Username-Password-Authentication")
				.setScope("openid email profile")
				.setAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
				.start(object : BaseCallback<Credentials, AuthenticationException> {
					override fun onSuccess(credentials: Credentials) {
                        val result = this@LoginActivity.verifyCredentials(credentials)
                        when (result) {
                            is Err -> { loginFailed(result.error) }
                            is Ok -> { loginSuccess(result.value) }
                        }
					}
					
					override fun onFailure(exception: AuthenticationException) {
						exception.printStackTrace()
                        if (exception.code == "invalid_grant") {
                            loginFailed(getString(R.string.incorrect_username_password))
                        }
                        else {
                            loginFailed(exception.description)
                        }
					}
				})
	}
	
	private fun loginFailed(errorMessage: String?) {
		runOnUiThread {
			loginProgress.visibility = View.INVISIBLE
			loginButton.isEnabled = true
			loginEmailEditText.isEnabled = true
			loginPasswordEditText.isEnabled = true
			loginErrorTextView.text = errorMessage
			loginErrorTextView.visibility = View.VISIBLE
		}
		
	}
	
	private fun loginSuccess(userAuthResponse: UserAuthResponse) {
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.ID_TOKEN, userAuthResponse.idToken)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.GU_ID, userAuthResponse.guid)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.SITE, userAuthResponse.defaultSite)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.ACCESS_TOKEN, userAuthResponse.accessToken)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.EMAIL, userAuthResponse.email)
		MessageListActivity.startActivity(this@LoginActivity)
		finish()
	}

    private fun verifyCredentials(credentials: Credentials): Result<UserAuthResponse, String> {
        val token = credentials.idToken
        if (token == null) {
            return Err(getString(R.string.an_error_occurred))
        }

        // Parsing JWT Token
        val withoutSignature = token.substring(0, token.lastIndexOf('.') + 1)
        try {
            val untrusted = Jwts.parser().parseClaimsJwt(withoutSignature)
            if (untrusted.body[metaDataKey] != null) {
                val metadata = untrusted.body[metaDataKey]

                if (metadata != null && metadata is HashMap<*, *>) {
                    val defaultSite: String? = metadata["defaultSite"] as String?
                    val guid: String? = metadata["guid"] as String?
                    val email: String? = untrusted.body["email"] as String?

                    when {
                        defaultSite.isNullOrEmpty() -> {
                            return Err(getString(R.string.user_are_not_ranger))
                        }
                        guid.isNullOrEmpty() || email.isNullOrEmpty() -> return Err(getString(R.string.an_error_occurred))

                        else -> return Ok(UserAuthResponse(email!!, guid!!, defaultSite!!, token, credentials.accessToken!!))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Err(getString(R.string.an_error_occurred))
    }
	
	private fun isLogin(): Boolean {
		return PreferenceHelper.getInstance(this@LoginActivity).getString(PrefKey.ID_TOKEN, "").isNotEmpty()
	}
}

data class UserAuthResponse (val email: String, val guid: String, val defaultSite: String, val idToken: String, val accessToken: String)