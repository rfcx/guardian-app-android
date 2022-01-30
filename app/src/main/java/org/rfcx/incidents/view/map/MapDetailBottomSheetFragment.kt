package org.rfcx.incidents.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentDialogReportDetailBinding
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.report.Report
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.limitDecimalPlace
import org.rfcx.incidents.util.toTimeSinceString
import org.rfcx.incidents.view.base.BaseFragment
import org.rfcx.incidents.view.report.getLocalisedValue

class MapDetailBottomSheetFragment : BaseFragment() {
    private var _binding: FragmentDialogReportDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MapDetailViewModel by viewModel()
    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDialogReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reportId: Int = arguments?.getInt("BUNDLE_REPORT_ID") ?: -1
        viewModel.getReportDetail(reportId).observe(
            viewLifecycleOwner
        ) {
            bindReportView(it)
        }

        viewModel.getReportImages(reportId).observe(
            viewLifecycleOwner
        ) {
            bindImageState(it)
        }
    }

    private fun bindReportView(report: Report?) {
        if (report != null) {
            binding.reportTypeNameTextView.text = context?.let { report.getLocalisedValue(it) }
            val latLon = StringBuilder(
                report.latitude.limitDecimalPlace(6)
            )
                .append(",")
                .append(report.longitude.limitDecimalPlace(6))
            binding.reportLocationTextView.text = latLon
            binding.reportTypeImageView.setImageResource(
                when (report.value) {
                    Event.vehicle -> R.drawable.ic_vehicle
                    Event.trespasser -> R.drawable.ic_people
                    Event.chainsaw -> R.drawable.ic_chainsaw
                    Event.gunshot -> R.drawable.ic_gun
                    else -> R.drawable.ic_pin_huge
                }
            )
            binding.reportTimePastedTextView.text = report.reportedAt.toTimeSinceString(context)

            binding.seeDetailTextView.setOnClickListener {
                analytics?.trackSeeReportDetailEvent(report.id.toString(), report.value)
            }
        }
    }

    private fun bindImageState(state: ImageState) {
        if (state.count == 0) {
            binding.reportImageStateTextView.visibility = View.INVISIBLE
            return
        }

        binding.reportImageStateTextView.visibility = View.VISIBLE
        if (state.unsentCount == 0) {
            binding.reportImageStateTextView.text = getString(
                R.string.images_sync_format, state.count
            )
        } else {
            binding.reportImageStateTextView.text = getString(
                R.string.images_unsync_format, state.count, state.unsentCount
            )
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
