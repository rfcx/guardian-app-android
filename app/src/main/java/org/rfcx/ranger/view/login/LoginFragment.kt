package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.facebookLoginButton
import kotlinx.android.synthetic.main.fragment_login.loginEmailEditText
import kotlinx.android.synthetic.main.fragment_login.loginPasswordEditText
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class LoginFragment : BaseFragment() {
    lateinit var listener: LoginListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as LoginListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        facebookLoginButton.setOnClickListener {
            listener.onLoginWithFacebook()
        }

        signInButton.setOnClickListener {
            val email = loginEmailEditText.text.toString()
            val password = loginPasswordEditText.text.toString()
            if (validateInput(email, password)) {
                listener.doLogin(email, password)
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
}