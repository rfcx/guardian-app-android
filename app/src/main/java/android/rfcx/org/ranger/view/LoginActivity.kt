package android.rfcx.org.ranger.view

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.rfcx.org.ranger.BuildConfig
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.repo.api.LoginApi
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

        LoginApi().login(this@LoginActivity, email, password, 0, object : LoginApi.OnLoginCallback {
            override fun onFailed(t: Throwable?, message: String?) {
                loginFailed(message)
            }

            override fun onSuccess(loginResponse: LoginResponse?) {
                loginSuccess()
            }

        })
    }

    private fun loginFailed(errorMessage: String?) {
        loginProgress.visibility = View.INVISIBLE
        loginButton.isEnabled = true
        loginEmailEditText.isEnabled = true
        loginPasswordEditText.isEnabled = true
        loginErrorTextView.text = errorMessage
        loginErrorTextView.visibility = View.VISIBLE
    }

    private fun loginSuccess() {
        MessageListActivity.startActivity(this@LoginActivity)
        finish()
    }
}
