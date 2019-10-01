package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class EmptyAlertFragment : BaseFragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_empty_alert, container, false)
	}
	
	companion object {
		const val tag = "EmptyAlertFragment"
		fun newInstance(): EmptyAlertFragment {
			return EmptyAlertFragment()
		}
	}
	
}