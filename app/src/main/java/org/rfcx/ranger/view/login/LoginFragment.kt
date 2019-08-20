package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class LoginFragment : BaseFragment() {
	lateinit var listener: LoginListener
	private val loginViewModel: LoginViewModel by viewModel()
	
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
		signInButton.setOnClickListener {
			val email = loginEmailEditText.text.toString()
			val password = loginPasswordEditText.text.toString()
			
			if (validateInput(email, password)) {
				loginGroupView.visibility = View.GONE
				loginProgressBar.visibility = View.VISIBLE
				loginViewModel.setLoginState()
				loginViewModel.doLogin(email, password)
				handleLogin()
			}
		}
		
		facebookLoginButton.setOnClickListener {
			loginGroupView.visibility = View.GONE
			loginProgressBar.visibility = View.VISIBLE
			loginViewModel.setLoginState()
			activity?.let { it1 -> loginViewModel.onLoginWithFacebook(it1) }
			handleLogin()
		}
	}
	
	private fun handleLogin() {
		loginViewModel.loginState.observe(this, Observer {
			when (it) {
				LoginState.SUCCESS -> {
					loginViewModel.loginResult.observe(this, Observer {
						loginViewModel.loginSuccess(it)
						handleUserTouch()
					})
				}
				LoginState.FAILED -> {
					loginViewModel.loginError.observe(this, Observer {
						loginGroupView.visibility = View.VISIBLE
						loginProgressBar.visibility = View.INVISIBLE
						loginViewModel.loginState.value
						Toast.makeText(context, it, Toast.LENGTH_LONG).show()
					})
				}
			}
		})
	}
	
	private fun getError() {
		loginViewModel.loginError.observe(this, Observer {
			loginGroupView.visibility = View.VISIBLE
			loginProgressBar.visibility = View.INVISIBLE
			Toast.makeText(context, it, Toast.LENGTH_LONG).show()
		})
	}
	
	private fun handleUserTouch() {
		loginViewModel.userTouchState.observe(this, Observer {
			when (it!!) {
				UserTouchState.SUCCESS -> {
					loginViewModel.gotoPage.observe(this, Observer {
						if (it == "MainActivityNew") {
							listener.openMain()
						} else if (it == "InvitationCodeFragment") {
							listener.openInvitationCodeFragment()
							Log.d("handleUserTouch", "InvitationCodeFragment")
						}
					})
				}
				UserTouchState.FAILED -> {
					getError()
				}
			}
		})
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