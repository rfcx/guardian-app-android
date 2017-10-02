package android.rfcx.org.ranger.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.rfcx.org.ranger.R
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

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
        loginButton.isEnabled = false
        loginEmailEditText.isEnabled = false
        loginPasswordEditText.isEnabled = false
    }
}
