package org.rfcx.incidents.view.events

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.stream.MarkerDetail
import org.rfcx.incidents.entity.stream.MarkerItem

class MarkerRenderer(
    private val context: Context, map: GoogleMap, clusterManager: ClusterManager<MarkerItem>
) : DefaultClusterRenderer<MarkerItem>(context, map, clusterManager) {
    private val mapMarkerView: MapMarkerView = MapMarkerView(context)
    private val markerIconGenerator = IconGenerator(context)

    init {
        markerIconGenerator.setBackground(null)
        markerIconGenerator.setContentView(mapMarkerView)
    }

    /**
     * Method called before the cluster item (the marker) is rendered.
     * This is where marker options should be set.
     */
    override fun onBeforeClusterItemRendered(
        item: MarkerItem, markerOptions: MarkerOptions
    ) {
        val data = Gson().fromJson(item.snippet, MarkerDetail::class.java)
        var drawable = if (data.countEvents == 0) {
            R.drawable.bg_circle_green
        } else {
            R.drawable.bg_circle_red
        }

        if (data.fromDeployment) {
            if (data.infoWindowMarker == null) return
            drawable = if (data.infoWindowMarker.isDeployment) {
                R.drawable.ic_pin_map
            } else {
                R.drawable.ic_pin_map_grey
            }
        }

        if (data.fromDeployment) {
            markerOptions.title(item.title).position(item.position).snippet(item.snippet)
                .icon(bitmapFromVector(context, drawable))
        } else {
            mapMarkerView.setContent(data.countEvents.toString(), drawable)
            markerOptions.title(item.title).position(item.position).snippet(item.snippet)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(context, mapMarkerView)))
        }
    }

    override fun getColor(clusterSize: Int): Int {
        return Color.parseColor("#2AA841")
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
        )
        val bitmap: Bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        val canvas: Canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun createDrawableFromView(context: Context, view: View): Bitmap {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        view.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        view.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * Method called right after the cluster item (the marker) is rendered.
     * This is where properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: MarkerItem, marker: Marker) {
        marker.tag = clusterItem
    }
}
