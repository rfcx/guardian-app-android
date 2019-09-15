package org.rfcx.ranger.view.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_set_user_name.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class SetUserNameFragment : BaseFragment() {
	
	lateinit var listener: LoginListener
	private val setUserNameViewModel: SetUserNameViewModel by viewModel()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as LoginListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_set_user_name, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
	}
	
	private fun initView() {
		submitButton.setOnClickListener {
			it.hideKeyboard()
			setNameProgressBar.visibility = View.VISIBLE
			
			val name = inputNameEditText.text.toString()
			setUserNameViewModel.sendName(name)
			setUserNameViewModel.userName.observe(this, Observer {
				if (it == "FAILED") {
					Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
				} else if (it.substring(0, 1) !== "+") {
					listener.openMain()
				}
			})
		}
	}
	
	private fun View.hideKeyboard() = this.let {
		val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		inputManager.hideSoftInputFromWindow(windowToken, 0)
	}
}