package org.rfcx.ranger.adapter.view

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.header_profile.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnLocationTrackingChangeListener

/**
 * View holder for the header profile section (which shows the location tracking status)
 */

class ProfileViewHolder(itemView: View, private val onLocationTrackingChangeListener: OnLocationTrackingChangeListener):
        RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, nickname: String, location: String, isLocationTracking: Boolean) {
        itemView.userNameTextView.text = context.getString(R.string.profile_welcome, nickname.trim().capitalize())
        itemView.locationTextView.text = location.capitalize()
        itemView.locationTrackingSwitch.isChecked = onLocationTrackingChangeListener.isEnableTracking()
        refresh(context, onLocationTrackingChangeListener.isEnableTracking())

        itemView.locationTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
            refresh(context, isChecked)
            onLocationTrackingChangeListener.onLocationTrackingChange(isChecked)
        }
    }

    private fun refresh(context: Context, isLocationTracking: Boolean) {
        itemView.statusImageView.setImageResource(if (isLocationTracking) R.drawable.ic_radar else R.drawable.ic_radar_grey)
        itemView.statusTextView.text = if (isLocationTracking) context.getString(R.string.profile_onduty) else context.getString(R.string.profile_nottracking)
        itemView.locationTextView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
        itemView.locationImageView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
    }
}