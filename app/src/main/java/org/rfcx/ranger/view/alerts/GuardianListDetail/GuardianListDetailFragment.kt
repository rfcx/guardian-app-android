package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class GuardianListDetailFragment : BaseFragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_guardian_list_detail, container, false)
	}
	
	companion object {
		const val tag = "GuardianListDetailFragment"
		fun newInstance(): GuardianListDetailFragment {
			return GuardianListDetailFragment()
		}
	}
}