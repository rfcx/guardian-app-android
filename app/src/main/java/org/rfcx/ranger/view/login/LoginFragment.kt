package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.util.isValidEmail
import org.rfcx.ranger.view.base.BaseFragment

class LoginFragment : BaseFragment() {
	lateinit var listener: LoginListener
	private val loginViewModel: LoginViewModel by viewModel()
	
	private val analytics by lazy { context?.let { Analytics(it) } }
	
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
		analytics?.trackScreen(Screen.LOGIN)
	}
	
	private fun initView() {
		signInButton.setOnClickListener {
			analytics?.trackLoginEvent("email")
			val email = loginEmailEditText.text.toString()
			val password = loginPasswordEditText.text.toString()
			it.hideKeyboard()
			
			if (validateInput(email, password)) {
				loading()
				loginViewModel.login(email, password)
			}
		}
		
		facebookLoginButton.setOnClickListener {
			loading()
			analytics?.trackLoginEvent("facebook")
			activity?.let { loginViewModel.loginWithFacebook(it) }
		}
		
		smsLoginButton.setOnClickListener {
			loading()
			analytics?.trackLoginEvent("sms")
			activity?.let { loginViewModel.loginMagicLink(it) }
		}
		
		forgotYourPasswordTextView.setOnClickListener {
			alertDialogResetPassword()
		}
	}
	
	private fun alertDialogResetPassword() {
		
		val builder = context?.let { AlertDialog.Builder(it) }
		val inflater = LayoutInflater.from(activity)
		val view = inflater.inflate(R.layout.reset_password_dialog, null)
		val editText = view.findViewById(R.id.emailResetPasswordEditText) as EditText
		val errorEmailFormat = view.findViewById(R.id.errorEmailFormatTextView) as TextView
		
		if (builder != null) {
			builder.setTitle(getString(R.string.reset_password))
			builder.setMessage(R.string.enter_email)
			builder.setView(view)
			builder.setCancelable(false)
			
			builder.setPositiveButton(getString(R.string.reset)) { _, _ ->
				loading()
				val email = editText.text.toString()
				loginViewModel.resetPassword(email)
			}
			
			builder.setNegativeButton(getString(R.string.cancel)) { _, _ ->
				view.hideKeyboard()
			}
			
			val alertDialog = builder.create()
			alertDialog.show()
			
			alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
			editText.addTextChangedListener(object : TextWatcher {
				override fun afterTextChanged(s: Editable?) {
					alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s?.length != 0
					if (s?.length != 0) {
						if (s.toString().isValidEmail()) {
							errorEmailFormat.visibility = View.INVISIBLE
						} else {
							errorEmailFormat.visibility = View.VISIBLE
						}
					} else {
						errorEmailFormat.visibility = View.INVISIBLE
					}
				}
				
				override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
				override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
			})
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
				LoginRedirect.SET_USER_NAME -> listener.openSetUserNameFragmentFragment()
				LoginRedirect.TERMS_AND_SERVICE -> listener.openTermsAndServiceFragment()
				LoginRedirect.SET_PROJECTS -> listener.openSetProjectsFragment()
				else -> loading(false)
			}
		})
		
		loginViewModel.resetPassword.observe(this, Observer { str ->
			if (str == SUCCESS) {
				loading(false)
				Toast.makeText(context, getString(R.string.reset_link_send), Toast.LENGTH_LONG).show()
			} else {
				loading(false)
				Toast.makeText(context, str, Toast.LENGTH_LONG).show()
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
	
	private fun View.hideKeyboard() = this.let {
		val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputManager.hideSoftInputFromWindow(windowToken, 0)
	}
	
	companion object {
		const val SUCCESS = "SUCCESS"
	}
}
