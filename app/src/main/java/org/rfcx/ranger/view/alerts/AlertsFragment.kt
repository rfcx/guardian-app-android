package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_alerts.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class AlertsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    companion object {
        const val tag = "AlertsFragment"
        fun newInstance(): AlertsFragment {
            return AlertsFragment()
        }
    }

    private fun initView() {
        alertsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position) {
                        0 -> {
                            startFragment(GroupAlertsFragment(), GroupAlertsFragment.tag)
                        }
                        1 -> {
                            startFragment(AllAlertsFragment.newInstance(), AllAlertsFragment.tag)
                        }
                    }
                }
            }
        })
        alertsTabLayout.getTabAt(1)?.select()
    }

    private fun startFragment(fragment: Fragment, tag: String) {
        childFragmentManager.beginTransaction()
                .replace(contentContainer.id, fragment,
                        tag).commit()
    }
}