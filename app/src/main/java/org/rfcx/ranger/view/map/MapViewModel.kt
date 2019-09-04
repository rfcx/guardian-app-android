package org.rfcx.ranger.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.asLiveData

class MapViewModel(private val reportDb: ReportDb, private val locationDb: LocationDb) : ViewModel() {
	
	fun getReports(): LiveData<List<Report>> {
		return Transformations.map(
				reportDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
	}
	
	fun getCheckIns(): LiveData<List<CheckIn>> {
		return Transformations.map(
				locationDb.allForDisplay().asLiveData()
		) {
			
			if (it.count() <= MAXIMUM_CHECK_IN_FOR_DISPLAY) {
				it
			} else {
				val displayPoints = arrayListOf<CheckIn>()
				val interestModPosition = it.count() / (MAXIMUM_CHECK_IN_FOR_DISPLAY)
				it.forEachIndexed { index, checkIn ->
					if (index % interestModPosition == 0) {
						displayPoints.add(checkIn)
					} else if (index == it.count() - 1) {
						displayPoints.add(checkIn)
					}
				}
				displayPoints
			}
		}
	}
	
	companion object {
		private const val MAXIMUM_CHECK_IN_FOR_DISPLAY = 10
	}
}