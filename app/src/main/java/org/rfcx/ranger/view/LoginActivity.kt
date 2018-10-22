package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.rfcx.ranger.R
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import io.jsonwebtoken.Jwts
import kotlinx.android.synthetic.main.activity_login.*


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
						credentials.idToken?.let {
							Log.d("credentials", credentials.idToken)
							
							// Parsing JWT Token
							val i = it.lastIndexOf('.')
							val withoutSignature = it.substring(0, i + 1)
							try {
								val untrusted = Jwts.parser().parseClaimsJwt(withoutSignature)
								if (untrusted.body[metaDataKey] != null) {
									val metadata = untrusted.body[metaDataKey]
									
									if (metadata != null && metadata is HashMap<*, *>) {
										val defaultSite: String? = metadata["defaultSite"] as String?
										val guid: String? = metadata["guid"] as String?
										
										when {
											defaultSite.isNullOrEmpty() -> {
												// this user is not Ranger
												loginFailed(getString(R.string.user_are_not_ranger))
											}
											guid.isNullOrEmpty() -> loginFailed(getString(R.string.an_error_occurred))
											
											else -> loginSuccess(email, guid!!, defaultSite!!, it, credentials.accessToken!!)
										}
									} else {
										loginFailed(getString(R.string.an_error_occurred))
									}
									
								}
							} catch (e: Exception) {
								e.printStackTrace()
                                loginFailed(getString(R.string.an_error_occurred))
							}
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
	
	private fun loginSuccess(email: String, guid: String, defaultSite: String, idToken: String, accessToken: String) {
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.ID_TOKEN, idToken)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.GU_ID, guid)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.SITE, defaultSite)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.ACCESS_TOKEN, accessToken)
		PreferenceHelper.getInstance(this@LoginActivity).putString(PrefKey.EMAIL, email)
		MessageListActivity.startActivity(this@LoginActivity)
		finish()
	}
	
	private fun isLogin(): Boolean {
		return PreferenceHelper.getInstance(this@LoginActivity).getString(PrefKey.ID_TOKEN, "").isNotEmpty()
	}
}
