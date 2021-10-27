package org.rfcx.ranger.view.login


import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_terms_and_service.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.view.base.BaseFragment

class TermsAndServiceFragment : BaseFragment() {
	lateinit var listener: LoginListener
	private val termsAndServiceViewModel: TermsAndServiceViewModel by viewModel()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as LoginListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_terms_and_service, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		termsAndConditionsWebview.webViewClient = object : WebViewClient() {
			override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
				super.onPageStarted(view, url, favicon)
				loadingTermsAndCondProgressBar.visibility = View.VISIBLE
			}
			
			override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
				if (url != null) {
					view?.loadUrl(url)
				}
				return true
			}
			
			override fun onPageFinished(view: WebView?, url: String?) {
				super.onPageFinished(view, url)
				loadingTermsAndCondProgressBar.visibility = View.GONE
			}
		}
		termsAndConditionsWebview.loadUrl("https://rfcx.org/terms-of-service-ranger-app-text-only")
		
		checkBox.setOnClickListener {
			submitButton.isEnabled = checkBox.isChecked
		}
		
		submitButton.setOnClickListener {
			termsAndServiceViewModel.acceptTerms()
		}
		
		termsAndServiceViewModel.consentGivenState.observe(this, Observer {
			it.success({ state ->
				if (state) {
					listener.handleOpenPage()
				}
			}, {
				termsProgressBar.visibility = View.GONE
				submitButton.visibility = View.VISIBLE
				Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_LONG).show()
			}, {
				termsProgressBar.visibility = View.VISIBLE
				submitButton.visibility = View.INVISIBLE
			})
		})
	}
}
