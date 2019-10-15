package org.rfcx.ranger.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_report_view_pager.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.MainActivityNew

class ReportViewPagerFragment : BottomSheetDialogFragment() {
	
	private val reportViewModel: ReportViewPagerFragmentViewModel by viewModel()
	private lateinit var viewPagerAdapter: ReportViewPagerAdapter
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_report_view_pager, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		initAdapter()
		
		reportViewModel.getReports(arguments?.getInt(BUNDLE_REPORT_ID)).observe(this, Observer {
			viewPagerAdapter.reports = it
			pageIndicatorView.count = if (viewPagerAdapter.itemCount >= 9) 9
			else viewPagerAdapter.itemCount
		})
	}
	
	private fun initAdapter() {
		viewPagerAdapter = ReportViewPagerAdapter(childFragmentManager, lifecycle)
		viewPager.adapter = viewPagerAdapter
		viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				if (activity is MainActivityNew) {
					(activity as MainActivityNew).moveMapIntoReportMarker(
							viewPagerAdapter.reports[position]
					)
				}
				pageIndicatorView.onPageSelected(position)
			}
			
			override fun onPageScrollStateChanged(state: Int) {
				pageIndicatorView.onPageScrollStateChanged(state)
			}
			
			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
				pageIndicatorView.onPageScrolled(position, positionOffset, positionOffsetPixels)
			}
		})
	}
	
	companion object {
		const val tag = "ReportViewPagerFragment"
		
		fun newInstance(reportId: Int): ReportViewPagerFragment {
			return ReportViewPagerFragment().apply {
				arguments = Bundle().apply {
					putInt(BUNDLE_REPORT_ID, reportId)
				}
			}
		}
		
		private const val BUNDLE_REPORT_ID = "BUNDLE_REPORT_ID"
	}
}