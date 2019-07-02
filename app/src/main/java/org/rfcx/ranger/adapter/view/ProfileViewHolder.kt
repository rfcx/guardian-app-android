package org.rfcx.ranger.adapter.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
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
    private val alertBar = itemView.layoutAlertBar
    private val tvUsername = itemView.userNameTextView
    private val tvLocation = itemView.locationTextView
    private val ivLocation = itemView.locationImageView
    private val switchTackingLocation = itemView.locationTrackingSwitch
    private val ivSyncState = itemView.ivSyncState
    private val tvSyncLabel = itemView.tvSyncLabel
    private val tvSyncDescription = itemView.tvSyncDescription
    private val tvNetworkState = itemView.tvNetworkState
    private val ivStatus = itemView.statusImageView
    private val tvStatus = itemView.statusTextView

    fun bind(context: Context, nickname: String, location: String) {

        // setup data
        val enableTracking = headerProtocol.isEnableTracking()
        val networkState = headerProtocol.getNetworkState()
        val syncInfo = headerProtocol.getSyncInfo()

        if (syncInfo != null && (syncInfo.countReport > 0 || syncInfo.countCheckIn > 2)) {
            updateAlertBar(syncInfo)
            alertBar.visibility = View.VISIBLE
        } else {
            alertBar.visibility = View.GONE
        }

        tvUsername.text = context.getString(R.string.profile_welcome, nickname.trim().capitalize())
        tvLocation.text = location.capitalize()
        switchTackingLocation.isChecked = enableTracking
        when (networkState) {
            NetworkState.ONLINE -> {
                setOnline()
            }
            NetworkState.OFFLINE -> {
                setOffline()
            }
        }
        refresh(context, enableTracking)

        switchTackingLocation.setOnCheckedChangeListener { _, isChecked ->
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
        val text = if (checkinText != null && reportText != null) "$reportText, $checkinText" else reportText
                ?: checkinText

        when (syncInfo.status) {
            SyncInfo.Status.WAITING_NETWORK -> {
                ivSyncState.setImageResource(R.drawable.ic_queue)
                tvSyncLabel.text = text
                tvSyncDescription.text = itemView.context.getString(R.string.sync_waiting_network)
            }
            SyncInfo.Status.STARTING -> {
                ivSyncState.setImageResource(R.drawable.ic_upload)
                tvSyncLabel.text = text
                tvSyncDescription.text = itemView.context.getText(R.string.sync_starting)
            }
            SyncInfo.Status.UPLOADING -> {
                ivSyncState.setImageResource(R.drawable.ic_upload)
                tvSyncLabel.text = text
                itemView.progressBarLoading.visibility = View.VISIBLE
                tvSyncDescription.text = itemView.context.getText(R.string.sync_uploading)
            }
            SyncInfo.Status.UPLOADED -> {
                handleUploaded()
            }
        }
    }

    private fun handleUploaded() {
        ivSyncState.setImageResource(R.drawable.ic_upload_done)
        tvSyncLabel.text = itemView.context.getString(R.string.sync_complete)
        tvSyncDescription.text = ""

        // if uploaded delay hide alert bar
        Handler().postDelayed({
            alertBar.visibility = View.GONE
        }, 2000) // delay 2s
    }

    private fun setOffline() {
        tvNetworkState.visibility = View.VISIBLE
        tvNetworkState.text = itemView.context.getString(R.string.network_offline)
        tvNetworkState.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey_default))
        tvNetworkState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_offline, 0, 0, 0)
    }

    private fun setOnline() {
        val delayHandler = Handler()
        val delayRunnable = Runnable { itemView.tvNetworkState?.visibility = View.INVISIBLE }

        tvNetworkState.text = itemView.context.getString(R.string.network_online)
        tvNetworkState.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
        tvNetworkState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_online, 0, 0, 0)

        delayHandler.postDelayed(delayRunnable, 3000)
    }

    private fun refresh(context: Context, isLocationTracking: Boolean) {
        ivStatus.setImageResource(if (isLocationTracking) R.drawable.ic_radar else R.drawable.ic_radar_grey)
        tvStatus.text = if (isLocationTracking) context.getString(R.string.profile_onduty) else context.getString(R.string.profile_nottracking)
        tvLocation.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
        ivLocation.visibility = if (isLocationTracking) View.VISIBLE else View.INVISIBLE
    }
}