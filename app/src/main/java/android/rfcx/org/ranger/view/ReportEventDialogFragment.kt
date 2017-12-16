package android.rfcx.org.ranger.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ReportType
import android.rfcx.org.ranger.entity.report.Attributes
import android.rfcx.org.ranger.entity.report.Data
import android.rfcx.org.ranger.entity.report.Report
import android.rfcx.org.ranger.entity.report.ReportData
import android.rfcx.org.ranger.repo.api.SendReportApi
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.isLocationAllow
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_report_evebt_dialog.*
import java.util.*


/**
 * Created by Jingjoeh on 12/16/2017 AD.
 */
class ReportEventDialogFragment : DialogFragment() {

    private var currentView: CurrentView = CurrentView.SELECT_TYPE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogDefaultStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_report_evebt_dialog, container, false);
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
                    // TODO report event
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


    @SuppressLint("MissingPermission")
    private fun sendReport(reportType: ReportType) {

        if (this.context!!.isLocationAllow()) {
            return
        }
        val time = DateHelper.getIsoTime()

        val fusedLocationClient = activity?.let { LocationServices.getFusedLocationProviderClient(it) }
        fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {

                // create body
                val reportAttributes = Attributes(time, time, task.result.longitude, task.result.longitude)
                val reportData = ReportData(UUID.randomUUID().toString(), reportType.name, reportAttributes)
                val data = Data(reportData)
                val report = Report(data)
                context?.let {
                    SendReportApi().sendReport(it, report, object : SendReportApi.SendReportCallback {
                        override fun onSuccess() {

                        }

                        override fun onFailed(t: Throwable?, message: String?) {

                        }
                    })
                }
            }
        }
    }

    enum class CurrentView {
        SELECT_TYPE, SELECT_SIGHT
    }

    companion object {
        val tag = "ReportEventDialogFragment"
        fun newInstance(): ReportEventDialogFragment {
            return ReportEventDialogFragment()
        }
    }
}