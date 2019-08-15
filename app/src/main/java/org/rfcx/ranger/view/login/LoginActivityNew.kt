package org.rfcx.ranger.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login_new.*
import kotlinx.android.synthetic.main.fragment_login.loginErrorTextView
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserAuthResponse
import org.rfcx.ranger.repo.api.UserTouchApi
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier
import org.rfcx.ranger.view.InvitationActivity
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
                .replace(loginContainer.id, LoginFragment(),
                        "LoginFragment").commit()
    }

    override fun onLoginWithFacebook() {

        // TODO doFacebookLogin()

        supportFragmentManager.beginTransaction()
                .replace(loginContainer.id, InvitationCodeFragment(),
                        "InvitationCodeFragment").commit()
    }

    override fun doLogin(email: String, password: String) {
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
                } else {
                    this@LoginActivityNew.startActivity(Intent(this@LoginActivityNew, InvitationActivity::class.java))
                }
                finish()
            }

            override fun onFailed(t: Throwable?, message: String?) {
                runOnUiThread {
                    loginProgress.visibility = View.INVISIBLE
                    loginGroupView.visibility = View.VISIBLE
                }
                Crashlytics.logException(t)
                loginFailed(message ?: t?.localizedMessage)
            }
        })
    }
}

interface LoginListener {
    fun onLoginWithFacebook()
    fun doLogin(email: String, password: String)
}
