package org.rfcx.ranger.view.status.adapter.viewholder

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.databinding.ItemStatusSyncingBinding
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class SyncingView(private val binding: ItemStatusSyncingBinding,val callback: SyncingViewCallback) : RecyclerView.ViewHolder(binding.root) {
	fun bind(syncInfoItem: StatusAdapter.SyncInfoItem) {
		binding.syncInfoItem = syncInfoItem
		binding.context = itemView.context
		handleUploaded(binding, syncInfoItem.syncInfo)
		binding.executePendingBindings()
	}
	
	private fun handleUploaded(binding: ItemStatusSyncingBinding, syncInfo: SyncInfo) {
		// if uploaded delay hide syncing view
		if (syncInfo.status == SyncInfo.Status.UPLOADED) {
			Handler().postDelayed({
				callback.onUploadCompleted()
			}, 2000) // delay 2s
		}
	}
}

interface SyncingViewCallback {
	fun onUploadCompleted()
}