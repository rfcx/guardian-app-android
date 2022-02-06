package org.rfcx.incidents.view.report.create

import android.content.Context
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.data.local.VoiceDb
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.location.toListDoubleArray
import org.rfcx.incidents.entity.response.ImageAsset
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.GeoJsonUtils
import java.util.Date

class CreateReportViewModel(
    private val responseDb: ResponseDb,
    private val voiceDb: VoiceDb,
    private val reportImageDb: ReportImageDb,
    private val trackingDb: TrackingDb,
    private val trackingFileDb: TrackingFileDb,
    private val eventDb: EventDb,
    private val streamDb: StreamDb
) : ViewModel() {

    fun getStream(id: String): Stream? {
        return streamDb.getStream(id)
    }

    fun getImagesFromLocal(id: Int): List<ImageAsset> = reportImageDb.getByReportId(id)

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
            val events = eventDb.getEvents(response.streamId)
            var point = t.points.toListDoubleArray()
            if (events.isNotEmpty()) {
                point = t.points.filter { p -> p.createdAt >= events[0].start }.toListDoubleArray()
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
