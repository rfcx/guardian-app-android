package org.rfcx.incidents.view.report.create

import android.content.Context
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.AssetDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.location.toListDoubleArray
import org.rfcx.incidents.entity.response.Asset
import org.rfcx.incidents.entity.response.AssetType
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.GeoJsonUtils
import java.util.Date

class CreateReportViewModel(
    private val responseDb: ResponseDb,
    private val trackingDb: TrackingDb,
    private val eventDb: EventDb,
    private val assetDb: AssetDb,
    private val streamDb: StreamDb
) : ViewModel() {

    fun getStream(id: String): Stream? {
        return streamDb.get(id)
    }

    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }

    fun saveAsset(asset: Asset): Asset {
        return assetDb.save(asset)
    }

    fun saveResponseInLocalDb(response: Response, images: List<String>?) {
        response.imagesAsset.forEach {
            assetDb.delete(it.id)
            response.assets.remove(it)
        }

        if (!images.isNullOrEmpty()) {
            images.forEach { path ->
                response.assets.add(assetDb.save(Asset(type = AssetType.IMAGE.value, localPath = path)))
            }
        }
        responseDb.save(response)
    }

    fun saveTrackingFile(response: Response, context: Context) {
        val track = trackingDb.getFirstTracking()
        track?.let { t ->
            val events = eventDb.getEvents(response.streamId)
            var point = t.points.toListDoubleArray()
            if (events.isNotEmpty()) {
                point = t.points.filter { p -> p.createdAt >= events[0].start }.toListDoubleArray()
            }
            val asset = Asset(
                type = AssetType.KML.value,
                localPath = GeoJsonUtils.generateGeoJson(
                    context,
                    GeoJsonUtils.generateFileName(response.submittedAt ?: Date()),
                    point
                ).absolutePath
            )
            response.assets.add(assetDb.save(asset))
        }
        trackingDb.deleteTracking(1, context)
    }

    fun getResponseById(id: Int): Response? = responseDb.getResponseById(id)
}
