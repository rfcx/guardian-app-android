package org.rfcx.ranger.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_set_user_name.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class SetUserNameFragment : BaseFragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_set_user_name, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
	}
	
	private fun initView() {
		submitButton.setOnClickListener {
			setNameProgressBar.visibility = View.VISIBLE
			
			val name = inputNameEditText.text.toString()
			
			Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
		}
	}
}