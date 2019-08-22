package org.rfcx.ranger.view.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_status.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.FragmentStatusBinding
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.view.MainActivityNew
import org.rfcx.ranger.view.base.BaseFragment
import org.rfcx.ranger.view.report.ReportDetailActivity
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class StatusFragment : BaseFragment(), StatusFragmentListener {
	
	private lateinit var viewDataBinding: FragmentStatusBinding
	private val statusViewModel: StatusViewModel by viewModel()
	private val statusAdapter by lazy {
		StatusAdapter(context?.getString(R.string.status_stat_title),
				context?.getString(R.string.status_report_title))
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		viewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_status, container, false)
		viewDataBinding.lifecycleOwner = this
		return viewDataBinding.root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewDataBinding.statusViewModel = statusViewModel // set view model
		
		// setup recycler view
		statusAdapter.setListener(this)
		rvStatus?.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = statusAdapter
		}
		
		statusViewModel.profile.observe(this, Observer {
			statusAdapter.updateHeader(it)
		})
		
		statusViewModel.summaryStat.observe(this, Observer {
			statusAdapter.updateStat(it)
		})
		
		statusViewModel.reportItems.observe(this, Observer {
			statusAdapter.updateReportList(it)
		})
		
		statusViewModel.syncInfo.observe(this, Observer {
			statusAdapter.updateSyncInfo(it)
		})
		
		statusViewModel.locationTracking.observe(this, Observer {
		
		})
	}
	
	override fun enableTracking(enable: Boolean) {
		if (enable) {
			// on location tracking
			(activity as MainActivityNew).enableLocationTracking {
				statusViewModel.updateTracking()
			}
		} else {
			// off location tracking
			(activity as MainActivityNew).disableLocationTracking()
			statusViewModel.updateTracking()
		}
	}
	
	override fun onClickedReportItem(report: Report) {
		// TODO: Start report detail page
		ReportActivity.startIntent(context, reportId = report.id)
	}
	
	companion object {
		fun newInstance(): StatusFragment {
			return StatusFragment()
		}
		
		const val tag = "StatusFragment"
	}
}

interface StatusFragmentListener {
	fun enableTracking(enable: Boolean)
	fun onClickedReportItem(report: Report)
}