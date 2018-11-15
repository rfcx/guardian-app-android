package org.rfcx.ranger.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.ReportSight
import org.rfcx.ranger.entity.report.ReportType
import org.rfcx.ranger.entity.report.Attributes
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportData
import org.rfcx.ranger.repo.api.SendReportApi
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.isLocationAllow
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_report_event_dialog.*
import java.util.*


/**
 * Created by Jingjoeh on 12/16/2017 AD.
 */
class ReportEventDialogFragment : BaseDialogFragment() {

    private var currentView: CurrentView = CurrentView.SELECT_TYPE
    private var onReportEventCallBack: OnReportEventCallBack? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnReportEventCallBack) {
            onReportEventCallBack = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogDefaultStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_event_dialog, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(currentView)

        reportPositiveButton.setOnClickListener {
            if (currentView == CurrentView.SELECT_TYPE) {

                if (radioReportGroup.checkedRadioButtonId == -1) {
                    reportHaveNoSelected.text = getString(R.string.report_no_have_select_type)
                    reportHaveNoSelected.visibility = View.VISIBLE
                } else {
                    reportHaveNoSelected.visibility = View.GONE
                    initView(CurrentView.SELECT_SIGHT)
                }

            } else {
                if (radioReportSight.checkedRadioButtonId == -1) {
                    reportHaveNoSelected.text = getString(R.string.report_no_have_select_sight)
                    reportHaveNoSelected.visibility = View.VISIBLE
                } else {
                    reportHaveNoSelected.visibility = View.INVISIBLE
                    validateInputToSendReport()
                }
            }
        }

        reportNegativeButton.setOnClickListener {
            if (currentView == CurrentView.SELECT_TYPE) {
                dismissAllowingStateLoss()
            } else {
                initView(CurrentView.SELECT_TYPE)
            }
        }
    }


    private fun initView(currentView: CurrentView) {
        this.currentView = currentView
        if (currentView == CurrentView.SELECT_TYPE) {
            radioReportGroup.visibility = View.VISIBLE
            radioReportSight.visibility = View.INVISIBLE
            reportPositiveButton.text = getString(R.string.report_select_sighting)
            reportNegativeButton.text = getString(R.string.cancel)
        } else {
            radioReportGroup.visibility = View.INVISIBLE
            radioReportSight.visibility = View.VISIBLE
            reportPositiveButton.text = getString(R.string.report_title)
            reportNegativeButton.text = getString(R.string.back)
        }
    }


    private fun validateInputToSendReport() {
        val reportType: ReportType? =
                when {
                    chainsawRadio.isChecked -> ReportType.chainsaw
                    gunshotRadio.isChecked -> ReportType.gunshot
                    trespasserRadio.isChecked -> ReportType.trespasser
                    vehicleRadio.isChecked -> ReportType.vehicle
                    else -> null
                }
        if (reportType == null) {
            initView(CurrentView.SELECT_TYPE)
            return
        }

        val reportSight: ReportSight? =
                when {
                    immediateRadio.isChecked -> ReportSight.Immediate
                    notFarAwayRadio.isChecked -> ReportSight.NotFarAway
                    veryFarRadio.isChecked -> ReportSight.VeryFar
                    else -> null
                }

        if (reportSight == null) {
            initView(CurrentView.SELECT_SIGHT)
            return
        }

        sendReport(reportType, reportSight)
    }


    @SuppressLint("MissingPermission")
    private fun sendReport(reportType: ReportType, reportSight: ReportSight) {

        if (context == null) return
        if (!context!!.isLocationAllow()) {
            return
        }
        showProgress()
        val time = DateHelper.getIsoTime()

        val fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }
        fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {

                // create body
                val distance = when (reportSight) {
                    ReportSight.Immediate -> 0
                    ReportSight.NotFarAway -> 50
                    ReportSight.VeryFar -> 100
                }
                val reportAttributes = Attributes(time, time, task.result!!.longitude, task.result!!.longitude, distance)
                val reportData = ReportData(UUID.randomUUID().toString(), reportType.name, reportAttributes)

                val report = Report(reportData)

                context?.let {
                    SendReportApi().sendReport(it, report, object : SendReportApi.SendReportCallback {
                        override fun onSuccess() {
                            hideProgress()
                            onReportEventCallBack?.onReportSuccess()
                            dismissAllowingStateLoss()
                        }

                        override fun onFailed(t: Throwable?, message: String?) {
                            hideProgress()
                            reportHaveNoSelected.text = message
                            reportHaveNoSelected.visibility = View.VISIBLE
                        }
                    })
                }
            }
        }
    }

    enum class CurrentView {
        SELECT_TYPE, SELECT_SIGHT
    }

    interface OnReportEventCallBack {
        fun onReportSuccess()
    }

    companion object {
        val tag = "ReportEventDialogFragment"
        fun newInstance(): ReportEventDialogFragment {
            return ReportEventDialogFragment()
        }
    }
}