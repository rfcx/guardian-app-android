package org.rfcx.ranger.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.realm.RealmResults
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.asLiveData

class MapViewModel(private val reportDb: ReportDb, private val locationDb: LocationDb) : ViewModel() {
	
	fun getReports(): LiveData<List<Report>> {
		return Transformations.map<RealmResults<Report>, List<Report>>(
				reportDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
	}
	
	fun getCheckIns(): LiveData<List<CheckIn>> {
		return Transformations.map<RealmResults<CheckIn>, List<CheckIn>>(
				locationDb.allForDisplay().asLiveData()
		) {
			it
		}
	}
}