package org.rfcx.ranger.adapter.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.header_profile.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.HeaderProtocol
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.service.NetworkState

/**
 * View holder for the header profile section (which shows the location tracking status)
 */

class ProfileViewHolder(itemView: View, private val headerProtocol: HeaderProtocol) :
        RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, nickname: String, location: String, isLocationTracking: Boolean) {

        // setup data
        val enableTracking = headerProtocol.isEnableTracking()
        val networkState = headerProtocol.getNetworkState()
        val syncInfo = headerProtocol.getSyncInfo()
        Log.d("Report", "ProfileViewHolder ${syncInfo?.status}")

        if (syncInfo != null && (syncInfo.countReport > 0 || syncInfo.countCheckIn > 2)) {
            updateAlertBar(syncInfo)
            itemView.layoutAlertBar.visibility = View.VISIBLE
        } else {
            itemView.layoutAlertBar.visibility = View.GONE
        }

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
            headerProtocol.onLocationTrackingChange(isChecked)
        }

//        itemView.cancelButton.setOnClickListener { headerProtocol.onPressCancelSync() }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAlertBar(syncInfo: SyncInfo) {
        itemView.progressBarLoading.visibility = View.INVISIBLE
//        itemView.cancelButton.visibility = View.VISIBLE

        val checkinText = if (syncInfo.countCheckIn > 1) itemView.context.getString(R.string.sync_checkins_label, syncInfo.countCheckIn) else null
        val reportText = if (syncInfo.countReport > 0) {
            itemView.context.getString(
                    if (syncInfo.countReport > 1) R.string.sync_reports_label else R.string.sync_report_label, syncInfo.countReport)
        } else null
        val text = if (checkinText != null && reportText != null) "$reportText, $checkinText" else reportText ?: checkinText

        when (syncInfo.status) {
            SyncInfo.Status.WAITING_NETWORK -> {
                itemView.ivSyncState.setImageResource(R.drawable.ic_queue)
                itemView.tvSyncLabel.text = text
                itemView.tvSyncDescription.text = itemView.context.getString(R.string.sync_waiting_network)
            }
            SyncInfo.Status.STARTING -> {
                itemView.ivSyncState.setImageResource(R.drawable.ic_upload)
                itemView.tvSyncLabel.text = text
                itemView.tvSyncDescription.text = itemView.context.getText(R.string.sync_starting)
            }
            SyncInfo.Status.UPLOADING -> {
                itemView.ivSyncState.setImageResource(R.drawable.ic_upload)
                itemView.tvSyncLabel.text = text
                itemView.progressBarLoading.visibility = View.VISIBLE
                itemView.tvSyncDescription.text = itemView.context.getText(R.string.sync_uploading)
            }
            SyncInfo.Status.UPLOADED -> {
                itemView.ivSyncState.setImageResource(R.drawable.ic_upload_done)
                itemView.tvSyncLabel.text = itemView.context.getString(R.string.sync_complete)
                itemView.tvSyncDescription.text = ""
//                itemView.cancelButton.visibility = View.INVISIBLE
            }
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

        delayHandler.postDelayed(delayRunnable, 3000)
    }

    private fun refresh(context: Context, isLocationTracking: Boolean) {
        itemView.statusImageView.setImageResource(if (isLocationTracking) R.drawable.ic_radar else R.drawable.ic_radar_grey)
        itemView.statusTextView.text = if (isLocationTracking) context.getString(R.string.profile_onduty) else context.getString(R.string.profile_nottracking)
        itemView.locationTextView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
        itemView.locationImageView.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
    }
}