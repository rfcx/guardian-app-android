package org.rfcx.ranger.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.api.InvitationCodeApi
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier
import org.rfcx.ranger.util.Preferences

class InvitationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invitation)
        setupWidgetView()
    }

    private fun setupWidgetView() {
        val inputInvitationCode = findViewById<EditText>(R.id.input_invitation_code)
        val nextButton = findViewById<FloatingActionButton>(R.id.button_next)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        nextButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            inputInvitationCode.isEnabled = false
            nextButton.isEnabled = false

            submit(inputInvitationCode.text.toString()) { success ->

                if (success) {
                    MessageListActivity.startActivity(this@InvitationActivity)
                    finish()
                }
                else {
                    progressBar.visibility = View.GONE
                    inputInvitationCode.isEnabled = true
                    nextButton.isEnabled = true
                    Toast.makeText(this@InvitationActivity, "Problem", Toast.LENGTH_LONG).show() // TODO: handle error
                }
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
                val result = CredentialVerifier(this@InvitationActivity).verify(credentials)
                when (result) {
                    is Err -> { callback(false) }
                    is Ok -> {
                        val userAuthResponse = result.value
                        if (userAuthResponse.isRanger) {
                            CredentialKeeper(this@InvitationActivity).save(userAuthResponse)
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

    private val auth0 by lazy {
        val auth0 = Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain))
        auth0.isOIDCConformant = true
        auth0
    }

    private val authentication by lazy {
        AuthenticationAPIClient(auth0)
    }

}
