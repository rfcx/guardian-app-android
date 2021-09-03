package org.rfcx.ranger.view.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.site.GetSiteNameUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.site.SiteResponse
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.asLiveData

class MapViewModel(private val profileData: ProfileData, private val reportDb: ReportDb, private val locationDb: LocationDb, private val eventDb: EventDb, private val getBounds: GetSiteNameUseCase) : ViewModel() {
	
	private val _boundaryCoordinates = MutableLiveData<ArrayList<ArrayList<Point>>>()
	val boundaryCoordinates: LiveData<ArrayList<ArrayList<Point>>> = _boundaryCoordinates
	
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
	
	fun getSiteBounds() {
		getBounds.execute(object : DisposableSingleObserver<List<SiteResponse>>() {
			override fun onSuccess(t: List<SiteResponse>) {
				if (t[0].bounds != null) {
					_boundaryCoordinates.value = t[0].toPointArray()
				}
			}
			
			override fun onError(e: Throwable) {
				Log.d("getSiteName", "error $e")
			}
		}, profileData.getSiteId())
	}
	
	fun SiteResponse.toPointArray(): ArrayList<ArrayList<Point>> {
		val coordinates: ArrayList<ArrayList<Point>> = arrayListOf()
		for ((index, value) in this.bounds.coordinates.withIndex()) {
			for ((index, value1) in value.withIndex()) {
				coordinates.add(ArrayList(value1.map { Point.fromLngLat(it[0], it[1]) }))
			}
		}
		return coordinates
	}
	
	companion object {
		private const val MAX_AVERAGE_CHECK_IN_FOR_DISPLAY = 120
	}
}
