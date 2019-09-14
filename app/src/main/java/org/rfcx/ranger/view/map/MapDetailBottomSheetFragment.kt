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
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.getPastedTimeFormat
import org.rfcx.ranger.view.report.ReportDetailActivity
import org.rfcx.ranger.view.report.getLocalisedValue

class MapDetailBottomSheetFragment : BottomSheetDialogFragment() {
	
	private val viewModel: MapDetailViewModel by viewModel()
	
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_dialog_report_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val reportId: Int = arguments?.getInt("BUNDLE_REPORT_ID") ?: -1
		viewModel.getReportDetail(reportId).observe(this@MapDetailBottomSheetFragment, Observer {
			bindReportView(it)
		})
		
		viewModel.getReportImages(reportId).observe(this@MapDetailBottomSheetFragment, Observer {
			bindImageState(it)
		})
	}
	
	
	private fun bindReportView(report: Report?) {
		// TODO move to use Data binding
		if (report == null) {
		
		} else {
			reportTypeNameTextView.text = context?.let { report.getLocalisedValue(it) }
			val latLon = StringBuilder(report.latitude.toString())
					.append(",")
					.append(report.longitude)
			reportLocationTextView.text = latLon
			reportTypeImageView.setImageResource(
					when (report.value) {
						Event.vehicle -> R.drawable.ic_vehicle
						Event.trespasser -> R.drawable.ic_people
						Event.chainsaw -> R.drawable.ic_chainsaw
						Event.gunshot -> R.drawable.ic_gun
						else -> R.drawable.ic_pin_huge
					}
			)
			val reportPasted = DateHelper.getTimePasted(report.reportedAt)
			reportTimePastedTextView.text = context.getPastedTimeFormat(reportPasted)
			
			seeDetailTextView.setOnClickListener {
				ReportDetailActivity.startIntent(context, reportId = report.id)
			}
		}
	}
	
	private fun bindImageState(state: ImageState) {
		// TODO move to use Data binding
		if (state.count == 0) {
			reportImageStateTextView.visibility = View.INVISIBLE
			return
		}
		
		reportImageStateTextView.visibility = View.VISIBLE
		if (state.unsentCount == 0) {
			reportImageStateTextView.text = getString(
					R.string.images_sync_format, state.count)
		} else {
			reportImageStateTextView.text = getString(
					R.string.images_unsync_format, state.count, state.unsentCount)
		}
	}
	
	companion object {
		fun newInstance(reportId: Int): MapDetailBottomSheetFragment {
			return MapDetailBottomSheetFragment().apply {
				arguments = Bundle().apply {
					putInt(BUNDLE_REPORT_ID, reportId)
				}
			}
		}
		
		const val tag = "ReportDetailBottomSheetFragment"
		private const val BUNDLE_REPORT_ID = "BUNDLE_REPORT_ID"
		
	}
}