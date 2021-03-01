package org.rfcx.ranger.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.asLiveData

class MapViewModel(private val reportDb: ReportDb, private val locationDb: LocationDb, private val eventDb: EventDb) : ViewModel() {
	
	fun getAlerts(): LiveData<List<Event>> {
		return Transformations.map(
				eventDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
	}
	
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
			
			if (it.count() <= MAX_AVERAGE_CHECK_IN_FOR_DISPLAY) {
				it
			} else {
				val displayPoints = arrayListOf<CheckIn>()
				val interestModPosition = it.count() / (MAX_AVERAGE_CHECK_IN_FOR_DISPLAY)
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
		private const val MAX_AVERAGE_CHECK_IN_FOR_DISPLAY = 120
	}
}
