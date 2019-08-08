package org.rfcx.ranger.view.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.adapter.entity.TitleItem
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class StatusViewModel(private val reportDb: ReportDb) : ViewModel() {
    private val items = MutableLiveData<List<StatusAdapter.StatusItemBase>>()
    val profile = MutableLiveData<StatusAdapter.ProfileItem>()
    val userStatus = MutableLiveData<StatusAdapter.UserStatusItem>()
    val reports = MutableLiveData<List<StatusAdapter.ReportItem>>()

//    val adapter = MutableLiveData<StatusAdapter>()
//    val layoutManager = MutableLiveData<RecyclerView.LayoutManager>()

    init {

        // TBD - demo ui
        userStatus.value = StatusAdapter.UserStatusItem(120, 10, 0)
        reports.value = Data.getSampleReports()
    }

    fun updateProfile(nickname: String, location: String, isLocationTracking: Boolean) {
        this.profile.value = StatusAdapter.ProfileItem(nickname, location, isLocationTracking)
        notifyDataChange()
    }

    private fun notifyDataChange() {
        val data = arrayListOf<StatusAdapter.StatusItemBase>()
        val dataProfile = profile.value
        val dataUserStatus = userStatus.value
        val dataReports = reports.value
        dataProfile?.let { data.add(it) } // profile data

        data.add(TitleItem("Your Status"))
        dataUserStatus?.let { data.add(it) } // user status data

        data.add(TitleItem("Report History"))
        dataReports?.forEach {
            data.add(it)
        } // report data

        items.value = data
    }

    fun getItems(): LiveData<List<StatusAdapter.StatusItemBase>> = items

    // TBD - demo ui
    object Data {
        fun getSampleReports(): List<StatusAdapter.ReportItem> {
            val report1 = Report(id = 0, guid = "xxxx", value = "vehicle", syncState = 0, latitude = 34.595, longitude = 213.508, reportedAt = "2019-08-08 06:50:00")
            val report2 = Report(id = 1, guid = "yyyyy", value = "vehicle", syncState = 0, latitude = 33.595, longitude = 213.508, reportedAt = "2019-08-08 10:00:30")
            val report3 = Report(id = 2, guid = "zzzzzz", value = "trespasser", syncState = 2, latitude = 36.595, longitude = 213.508, reportedAt = "2019-08-08 14:00:30")
            val reportItems = arrayListOf<StatusAdapter.ReportItem>()
            reportItems.add(StatusAdapter.ReportItem(report1, 2, 0))
            reportItems.add(StatusAdapter.ReportItem(report2, 0, 0))
            reportItems.add(StatusAdapter.ReportItem(report3, 3, 0))
            return reportItems
        }
    }
}