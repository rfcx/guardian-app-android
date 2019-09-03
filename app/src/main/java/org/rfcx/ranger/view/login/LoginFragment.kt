package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
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
		setupObserver()
	}
	
	override fun onResume() {
		super.onResume()
		loading(false)
	}
	
	private fun initView() {
		signInButton.setOnClickListener {
			val email = loginEmailEditText.text.toString()
			val password = loginPasswordEditText.text.toString()
			
			if (validateInput(email, password)) {
				loading()
				loginViewModel.login(email, password)
			}
		}
		
		facebookLoginButton.setOnClickListener {
			loading()
			activity?.let { it1 -> loginViewModel.loginWithFacebook(it1) }
		}
	}
	
	private fun loading(start: Boolean = true) {
		loginGroupView.visibility = if (start) View.GONE else View.VISIBLE
		loginProgressBar.visibility = if (start) View.VISIBLE else View.GONE
	}
	
	private fun setupObserver() {
		loginViewModel.userAuth.observe(this, Observer {
			loading()
			it ?: return@Observer
			loginViewModel.checkUserDetail(it)
		})
		
		loginViewModel.loginFailure.observe(this, Observer { errorMessage ->
			if (errorMessage != null && errorMessage.isNotEmpty()) {
				Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
			}
			loading(false)
		})
		
		loginViewModel.redirectPage.observe(this, Observer { loginRedirect ->
			when (loginRedirect) {
				LoginRedirect.MAIN_PAGE -> listener.openMain()
				LoginRedirect.INVITE_CODE_PAGE -> listener.openInvitationCodeFragment()
				else -> loading(false)
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