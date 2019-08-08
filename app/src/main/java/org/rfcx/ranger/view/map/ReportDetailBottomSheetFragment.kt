package org.rfcx.ranger.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.report.Report

class ReportDetailBottomSheetFragment : BottomSheetDialogFragment() {
	
	private val reportViewModel: ReportDetailViewModel by viewModel()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_dialog_report_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val reportId: Int = arguments?.getInt("BUNDLE_REPORT_ID") ?: -1
		reportViewModel.getReportDetail(reportId).observe(this@ReportDetailBottomSheetFragment, Observer {
			bindReportView(it)
		})
		
		reportViewModel.getReportImages(reportId).observe(this@ReportDetailBottomSheetFragment, Observer {
			bindImageState(it)
		})
	}
	
	
	private fun bindReportView(report: Report?) {
		// TODO move to use Data binding
		if (report == null) {
		
		} else {
			reportTypeNameTextView.text = report.value
			val latLon = StringBuilder(report.latitude.toString())
					.append(",")
					.append(report.longitude)
			reportLocationTextView.text = latLon
			reportTypeImageView.setImageResource(
					when (report.value) {
						Event.vehicle -> R.drawable.ic_truck
						Event.trespasser -> R.drawable.ic_people
						Event.chainsaw -> R.drawable.ic_chainsaw
						Event.gunshot -> R.drawable.ic_gun
						else -> R.drawable.ic_other
					}
			)
		}
	}
	
	private fun bindImageState(state: ImageState) {
		// TODO move to use Data binding
		if (state.unsentCount == 0) {
			reportImageStateTextView.text = getString(
					R.string.images_sync_format, state.count)
		} else {
			reportImageStateTextView.text = getString(
					R.string.images_unsync_format, state.count, state.unsentCount)
		}
	}
	
	companion object {
		fun newInstance(reportId: Int): ReportDetailBottomSheetFragment {
			return ReportDetailBottomSheetFragment().apply {
				arguments = Bundle().apply {
					putInt(BUNDLE_REPORT_ID, reportId)
				}
			}
		}
		
		const val tag = "ReportDetailBottomSheetFragment"
		private const val BUNDLE_REPORT_ID = "BUNDLE_REPORT_ID"
		
	}
}