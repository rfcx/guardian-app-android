package org.rfcx.incidents.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.location.CheckIn
import org.rfcx.incidents.entity.report.Report
import org.rfcx.incidents.localdb.LocationDb
import org.rfcx.incidents.localdb.ReportDb
import org.rfcx.incidents.util.asLiveData

class MapViewModel(
    private val reportDb: ReportDb,
    private val locationDb: LocationDb
) : ViewModel() {

    private val _boundaryCoordinates = MutableLiveData<ArrayList<ArrayList<Point>>>()
    val boundaryCoordinates: LiveData<ArrayList<ArrayList<Point>>> = _boundaryCoordinates

    fun getAlerts(): LiveData<List<Event>> {
        return MutableLiveData() // TODO
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

    fun getSiteBounds() {
        // TODO
    }

    companion object {
        private const val MAX_AVERAGE_CHECK_IN_FOR_DISPLAY = 120
    }
}
