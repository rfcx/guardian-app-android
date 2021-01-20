package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_invitation_code.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.util.getUserNickname
import org.rfcx.ranger.view.base.BaseFragment

class InvitationCodeFragment : BaseFragment() {
	lateinit var listener: LoginListener
	private val invitationCodeViewModel: InvitationCodeViewModel by viewModel()
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as LoginListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_invitation_code, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.INVITECODE)
	}
	
	private fun initView() {
		inputCodeEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(p0: Editable?) {
				if (p0 != null) {
					if (p0.isEmpty()) {
						submitButton.isEnabled = false
					}
				}
			}
			
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
				submitButton.isEnabled = true
			}
		})
		
		submitButton.setOnClickListener {
			submitButton.isEnabled = false
			invitationProgressBar.visibility = View.VISIBLE
			it.hideKeyboard()
			
			val code = inputCodeEditText.text.toString()
			analytics?.trackEnterInviteCodeEvent(code)
			
			invitationCodeViewModel.setSubmitState()
			invitationCodeViewModel.doSubmit(code)
			invitationCodeViewModel.submitCodeState.observe(this, Observer { state ->
				when (state) {
					SubmitState.SUCCESS -> {
						listener.handleOpenPage()
					}
					SubmitState.FAILED -> {
						invitationProgressBar.visibility = View.GONE
						submitButton.isEnabled = true
						Toast.makeText(context, R.string.invalid_invite_code, Toast.LENGTH_LONG).show()
					}
				}
			})
		}
		
		switchAccountTextView.setOnClickListener {
			listener.openLoginFragment()
		}
	}
	
	private fun View.hideKeyboard() = this.let {
		val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputManager.hideSoftInputFromWindow(windowToken, 0)
	}
}
