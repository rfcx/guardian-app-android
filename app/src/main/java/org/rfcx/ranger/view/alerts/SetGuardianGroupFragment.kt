package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_set_guardian_group.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment
import org.rfcx.ranger.view.profile.GuardianGroupActivity

class SetGuardianGroupFragment : BaseFragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_set_guardian_group, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setGuardianGroupButton.setOnClickListener {
			context?.let { it1 -> GuardianGroupActivity.startActivity(it1) }
		}
	}
}