package org.rfcx.ranger.adapter.view

import android.content.Context
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.header_profile.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnLocationTrackingChangeListener
import org.rfcx.ranger.service.NetworkState

/**
 * View holder for the header profile section (which shows the location tracking status)
 */

class ProfileViewHolder(itemView: View, private val onLocationTrackingChangeListener: OnLocationTrackingChangeListener) :
        RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, nickname: String, location: String, isLocationTracking: Boolean) {

        // setup data
        val enableTracking = onLocationTrackingChangeListener.isEnableTracking()
        val networkState = onLocationTrackingChangeListener.getNetworkState()

        itemView.userNameTextView.text = context.getString(R.string.profile_welcome, nickname.trim().capitalize())
        itemView.locationTextView.text = location.capitalize()
        itemView.locationTrackingSwitch.isChecked = enableTracking
        when (networkState) {
            NetworkState.ONLINE -> {
                setOnline()
            }
            NetworkState.OFFLINE -> {
                setOffline()
            }
        }
        refresh(context, enableTracking)

        itemView.locationTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
            refresh(context, isChecked)
            onLocationTrackingChangeListener.onLocationTrackingChange(isChecked)
        }
    }

    private fun setOffline() {
        itemView.tvNetworkState.visibility = View.VISIBLE
        itemView.tvNetworkState.text = itemView.context.getString(R.string.network_offline)
        itemView.tvNetworkState.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey_default))
        itemView.tvNetworkState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_offline, 0, 0, 0)
    }

    private fun setOnline() {
        val delayHandler = Handler()
        val delayRunnable = Runnable { itemView.tvNetworkState?.visibility = View.INVISIBLE }

        itemView.tvNetworkState.text = itemView.context.getString(R.string.network_online)
        itemView.tvNetworkState.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
        itemView.tvNetworkState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_online, 0, 0, 0)

        delayHandler.postDelayed(delayRunnable, 5000)
    }

    private fun refresh(context: Context, isLocationTracking: Boolean) {
        itemView.statusImageView.setImageResource(if (isLocationTracking) R.drawable.ic_radar else R.drawable.ic_radar_grey)
        itemView.statusTextView.text = if (isLocationTracking) context.getString(R.string.profile_onduty) else context.getString(R.string.profile_nottracking)
        itemView.locationTextView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
        itemView.locationImageView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
    }
}