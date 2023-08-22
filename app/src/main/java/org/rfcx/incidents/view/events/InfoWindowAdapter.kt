package org.rfcx.incidents.view.events

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.stream.MarkerDetail
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.toStringWithTimeZone
import java.util.TimeZone

class InfoWindowAdapter(var mContext: Context) : GoogleMap.InfoWindowAdapter {
    var mWindow: View = LayoutInflater.from(mContext).inflate(R.layout.layout_deployment_window_info, null)

    @SuppressLint("SetTextI18n")
    private fun setInfoWindowText(marker: Marker) {
        if (marker.snippet == null) return
        val markerDetail = Gson().fromJson(marker.snippet, MarkerDetail::class.java)
        if (markerDetail.infoWindowMarker == null) return
        val data = markerDetail.infoWindowMarker

        val deploymentSiteTitle = mWindow.findViewById<TextView>(R.id.deploymentSiteTitle)
        val dateAt = mWindow.findViewById<TextView>(R.id.deployedAt)
        val dateLayout = mWindow.findViewById<LinearLayout>(R.id.dateLayout)
        val latLngTextView = mWindow.findViewById<TextView>(R.id.latLngTextView)
        val seeDeploymentDetail = mWindow.findViewById<LinearLayout>(R.id.seeDeploymentDetail)
        seeDeploymentDetail.visibility = if (data.isDeployment) View.VISIBLE else View.GONE
        val guardianNameTextView = mWindow.findViewById<TextView>(R.id.guardianNameTextView)
        guardianNameTextView.visibility = View.GONE

        deploymentSiteTitle.text = data.locationName
        if (data.isDeployment) {
            dateAt.text = data.deploymentAt?.toStringWithTimeZone(mContext, TimeZone.getDefault()) ?: ""
        } else {
            dateLayout.visibility = View.GONE
        }
        val latLngText = "${data.latitude.latitudeCoordinates()}, ${data.longitude.longitudeCoordinates()}"
        latLngTextView.text = latLngText
    }

    override fun getInfoWindow(p0: Marker): View {
        if (p0.snippet == null) return mWindow
        setInfoWindowText(p0)
        return mWindow
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindowText(p0)
        return mWindow
    }
}
