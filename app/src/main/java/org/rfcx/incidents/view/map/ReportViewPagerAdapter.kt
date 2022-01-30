package org.rfcx.incidents.view.map

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.rfcx.incidents.entity.report.Report

class ReportViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    var reports = listOf<Report>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = reports.count()

    override fun createFragment(position: Int): Fragment {
        return MapDetailBottomSheetFragment.newInstance(reports[position].id)
    }
}
