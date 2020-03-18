package org.rfcx.ranger.view.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_terms_and_service.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class TermsAndServiceFragment : BaseFragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_terms_and_service, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		termsAndConditionsWebview.loadUrl("https://rfcx.org/terms-of-service-text-only")
		
		checkBox.setOnClickListener {
			submitButton.isEnabled = checkBox.isChecked
		}
	}
	
}
