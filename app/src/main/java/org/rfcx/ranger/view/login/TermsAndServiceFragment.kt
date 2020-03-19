package org.rfcx.ranger.view.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_terms_and_service.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.view.MainActivityNew
import org.rfcx.ranger.view.base.BaseFragment

class TermsAndServiceFragment : BaseFragment() {
	
	private val termsAndServiceViewModel: TermsAndServiceViewModel by viewModel()
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_terms_and_service, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		termsAndConditionsWebview.loadUrl("https://rfcx.org/terms-of-service-ranger-app-text-only")
		
		checkBox.setOnClickListener {
			submitButton.isEnabled = checkBox.isChecked
		}
		
		submitButton.setOnClickListener {
			termsAndServiceViewModel.acceptTerms()
			
			termsAndServiceViewModel.consentGivenState.observe(this, Observer {
				it.success({ state ->
					if (state) {
						context?.let { it1 -> MainActivityNew.startActivity(it1, null) }
					}
				}, {
					termsProgressBar.visibility = View.GONE
					submitButton.visibility = View.VISIBLE
					
				}, {
					termsProgressBar.visibility = View.VISIBLE
					submitButton.visibility = View.INVISIBLE
				})
			})
		}
	}
}
