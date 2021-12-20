package org.rfcx.incidents.view.report.create

import android.content.Context
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.location.toListDoubleArray
import org.rfcx.incidents.entity.report.ReportImage
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.localdb.*
import org.rfcx.incidents.util.GeoJsonUtils
import java.util.*

class CreateReportViewModel(private val responseDb: ResponseDb, private val voiceDb: VoiceDb, private val reportImageDb: ReportImageDb, private val trackingDb: TrackingDb, private val trackingFileDb: TrackingFileDb, private val alertDb: AlertDb) : ViewModel() {
	
	fun getImagesFromLocal(id: Int): List<ReportImage> = reportImageDb.getByReportId(id)
	
	fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
		trackingDb.insertOrUpdate(tracking, coordinate)
	}
	
	fun saveResponseInLocalDb(response: Response, images: List<String>?) {
		val res = responseDb.save(response)
		if (!images.isNullOrEmpty()) {
			reportImageDb.deleteImages(res.id)
			reportImageDb.save(res, images)
		}
		voiceDb.save(res)
	}
	
	fun saveTrackingFile(response: Response, context: Context) {
		val track = trackingDb.getFirstTracking()
		track?.let { t ->
			val alerts = alertDb.getAlerts(response.streamId)
			var point = t.points.toListDoubleArray()
			if (alerts.isNotEmpty()) {
				point = t.points.filter { p -> p.createdAt >= alerts[0].start }.toListDoubleArray()
			}
			val trackingFile = TrackingFile(
					responseId = response.id,
					streamServerId = response.streamId,
					localPath = GeoJsonUtils.generateGeoJson(
							context,
							GeoJsonUtils.generateFileName(response.submittedAt ?: Date()),
							point
					).absolutePath
			)
			trackingFileDb.insertOrUpdate(trackingFile)
		}
		trackingDb.deleteTracking(1, context)
	}
	
	fun getResponseById(id: Int): Response? = responseDb.getResponseById(id)
}
