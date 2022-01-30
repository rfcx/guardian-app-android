package org.rfcx.incidents.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.entity.report.Report
import org.rfcx.incidents.localdb.ReportDb
import org.rfcx.incidents.util.asLiveData

class ReportViewPagerFragmentViewModel(private val reportDb: ReportDb) : ViewModel() {
    
    fun getReports(): LiveData<List<Report>> {
        return Transformations.map(
            reportDb.getAllResultsAsync().asLiveData()
        ) {
            it
        }
    }
}
