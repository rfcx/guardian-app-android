package org.rfcx.ranger.view.login

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_login_new.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.loginErrorTextView
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserAuthResponse
import org.rfcx.ranger.repo.api.InvitationCodeApi
import org.rfcx.ranger.repo.api.UserTouchApi
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.view.MainActivityNew


// TODO change class name
class LoginActivityNew : AppCompatActivity(), LoginListener {
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, LoginActivityNew::class.java)
			context.startActivity(intent)
		}
	}
	
	private val auth0 by lazy {
		val auth0 = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	private val webAuthentication by lazy {
		WebAuthProvider.init(auth0)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login_new)
		
		// TODO hasValidCredentials()
		
		supportFragmentManager.beginTransaction()
				.add(loginContainer.id, LoginFragment(),
						"LoginFragment").commit()
	}
	
	override fun onLoginWithFacebook() {
		loginGroupView.visibility = View.GONE
		loginProgressBar.visibility = View.VISIBLE
		loginErrorTextView.visibility = View.INVISIBLE
		
		webAuthentication
				.withConnection("facebook")
				.withScope(getString(R.string.auth0_scopes))
				.withScheme(getString(R.string.auth0_scheme))
				.withAudience(getString(R.string.auth0_audience))
				.start(this@LoginActivityNew, object : AuthCallback {
					override fun onFailure(dialog: Dialog) {
						loginFailed(null)
					}
					
					override fun onFailure(exception: AuthenticationException) {
						runOnUiThread{
							loginProgressBar.visibility = View.INVISIBLE
							loginGroupView.visibility = View.VISIBLE
						}
						Crashlytics.logException(exception)
						loginFailed(exception.localizedMessage)
					}
					
					override fun onSuccess(credentials: Credentials) {
						val result = CredentialVerifier(this@LoginActivityNew).verify(credentials)
						when (result) {
							is Err -> {
								loginFailed(result.error)
							}
							is Ok -> {
								loginSuccess(result.value)
							}
						}
					}
				})
	}
	
	override fun doLogin(email: String, password: String) {
		loginGroupView.visibility = View.GONE
		loginProgressBar.visibility = View.VISIBLE
		loginErrorTextView.visibility = View.INVISIBLE
		
		authentication
				.login(email, password, "Username-Password-Authentication")
				.setScope(getString(R.string.auth0_scopes))
				.setAudience(getString(R.string.auth0_audience))
				.start(object : BaseCallback<Credentials, AuthenticationException> {
					override fun onSuccess(credentials: Credentials) {
						val result = CredentialVerifier(this@LoginActivityNew).verify(credentials)
						when (result) {
							is Err -> {
								loginFailed(result.error)
							}
							is Ok -> {
								loginSuccess(result.value)
							}
						}
					}
					
					override fun onFailure(exception: AuthenticationException) {
						exception.printStackTrace()
						Crashlytics.logException(exception)
						if (exception.code == "invalid_grant") {
							loginFailed(getString(R.string.incorrect_username_password))
						} else {
							loginFailed(exception.description)
						}
					}
				})
	}
	
	private fun loginFailed(errorMessage: String?) {
		runOnUiThread {
			loginGroupView.visibility = View.VISIBLE
			loginProgressBar.visibility = View.INVISIBLE
			loginErrorTextView.text = errorMessage
			loginErrorTextView.visibility = View.VISIBLE
		}
	}
	
	private fun loginSuccess(userAuthResponse: UserAuthResponse) {
		
		CredentialKeeper(this@LoginActivityNew).save(userAuthResponse)
		
		UserTouchApi().send(this, object : UserTouchApi.UserTouchCallback {
			override fun onSuccess() {
				if (userAuthResponse.isRanger) {
					MainActivityNew.startActivity(this@LoginActivityNew)
					finish()
				} else {
					supportFragmentManager.beginTransaction()
							.replace(loginContainer.id, InvitationCodeFragment(),
									"InvitationCodeFragment").commit()
				}
				
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				runOnUiThread {
					loginProgressBar.visibility = View.INVISIBLE
					loginGroupView.visibility = View.VISIBLE
				}
				Crashlytics.logException(t)
				loginFailed(message ?: t?.localizedMessage)
			}
		})
	}
	
	override fun doSubmit(code: String) {
		submit(code) { success ->
			if (success) {
				MainActivityNew.startActivity(this@LoginActivityNew)
				finish()
			} else {
				Toast.makeText(this@LoginActivityNew, "Problem", Toast.LENGTH_LONG).show() // TODO: handle error
			}
		}
	}
	
	private fun submit(code: String, callback: (Boolean) -> Unit) {
		InvitationCodeApi().send(this, code, object : InvitationCodeApi.InvitationCodeCallback {
			override fun onSuccess() {
				refreshToken { success ->
					callback(success)
				}
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				callback(false)
			}
		})
	}
	
	private fun refreshToken(callback: (Boolean) -> Unit) {
		val refreshToken = Preferences.getInstance(this).getString(Preferences.REFRESH_TOKEN)
		if (refreshToken == null) {
			callback(false)
			return
		}
		
		authentication.renewAuth(refreshToken).start(object : BaseCallback<Credentials, AuthenticationException> {
			override fun onSuccess(credentials: Credentials) {
				val result = CredentialVerifier(this@LoginActivityNew).verify(credentials)
				when (result) {
					is Err -> {
						callback(false)
					}
					is Ok -> {
						val userAuthResponse = result.value
						if (userAuthResponse.isRanger) {
							CredentialKeeper(this@LoginActivityNew).save(userAuthResponse)
						}
						callback(userAuthResponse.isRanger)
					}
				}
			}
			
			override fun onFailure(error: AuthenticationException) {
				callback(false)
			}
		})
	}
}

interface LoginListener {
	fun onLoginWithFacebook()
	fun doLogin(email: String, password: String)
	fun doSubmit(code: String)
}
