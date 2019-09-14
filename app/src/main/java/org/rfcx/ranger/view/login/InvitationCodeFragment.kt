package org.rfcx.ranger.view.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_invitation_code.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class InvitationCodeFragment : BaseFragment() {
	lateinit var listener: LoginListener
	private val invitationCodeViewModel: InvitationCodeViewModel by viewModel()
	
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
	
	private fun initView() {
		submitButton.setOnClickListener {
			invitationProgressBar.visibility = View.VISIBLE
			it.hideKeyboard()
			
			val code = inputCodeEditText.text.toString()
			invitationCodeViewModel.setSubmitState()
			invitationCodeViewModel.doSubmit(code)
			invitationCodeViewModel.submitCodeState.observe(this, Observer {
				when (it) {
					SubmitState.SUCCESS -> {
						listener.openMain()
					}
					SubmitState.FAILED -> {
						invitationProgressBar.visibility = View.GONE
						Toast.makeText(context, R.string.invalid_invite_code, Toast.LENGTH_LONG).show() // TODO: handle error
					}
				}
			})
		}
	}
	
	private fun View.hideKeyboard() = this.let {
		val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputManager.hideSoftInputFromWindow(windowToken, 0)
	}
}