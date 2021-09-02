package org.rfcx.ranger.view.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class StatusFragment : BaseFragment() {
	companion object {
		const val tag = "StatusFragment"
		
		fun newInstance(): StatusFragment = StatusFragment()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_status, container, false)
	}
}
