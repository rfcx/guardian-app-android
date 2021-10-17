package org.rfcx.ranger.view.report.create

import android.content.Context
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.AlertDb
import org.rfcx.ranger.entity.location.TrackingFile
import org.rfcx.ranger.entity.location.toDoubleArray
import org.rfcx.ranger.entity.location.toListDoubleArray
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.localdb.TrackingDb
import org.rfcx.ranger.localdb.TrackingFileDb
import org.rfcx.ranger.util.GeoJsonUtils
import java.util.*

class CreateReportViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb, private val trackingDb: TrackingDb, private val trackingFileDb: TrackingFileDb, private val alertDb: AlertDb) : ViewModel() {
	
	fun getImagesFromLocal(id: Int): List<ReportImage> = reportImageDb.getByReportId(id)
	
	fun saveResponseInLocalDb(response: Response, images: List<String>?) {
		val res = responseDb.save(response)
		if (!images.isNullOrEmpty()) {
			reportImageDb.deleteImages(res.id)
			reportImageDb.save(res, images)
		}
	}
	
	fun saveTrackingFile(response: Response, context: Context) {
		val track = trackingDb.getFirstTracking()
		track?.let { t ->
			val alerts = alertDb.getAlerts(response.streamId)
			var point = t.points.toListDoubleArray()
			if (alerts.isNotEmpty()) {
				point = t.points.filter { p -> p.saveAt >= alerts[0].start }.toDoubleArray()
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
