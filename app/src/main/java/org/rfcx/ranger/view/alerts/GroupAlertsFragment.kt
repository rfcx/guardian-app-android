package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class GroupAlertsFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val tag = "GroupAlertsFragment"
        fun newInstance(): GroupAlertsFragment {
            return GroupAlertsFragment()
        }
    }
}