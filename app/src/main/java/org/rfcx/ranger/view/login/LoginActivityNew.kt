package org.rfcx.ranger.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login_new.*
import org.rfcx.ranger.R


// TODO change class name
class LoginActivityNew : AppCompatActivity(), LoginListener {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, LoginActivityNew::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_new)

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
}

interface LoginListener {
    fun onLoginWithFacebook()
}
