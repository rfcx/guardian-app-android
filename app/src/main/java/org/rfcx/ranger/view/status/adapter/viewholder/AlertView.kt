package org.rfcx.ranger.view.status.adapter.viewholder

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemStatusAlertBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class AlertView(private val binding: ItemStatusAlertBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.AlertItem) {
		binding.alertItem = item
		binding.context = binding.root.context
		binding.onClickedAlertItem = View.OnClickListener {
			listener?.onClickedAlertItem(item.alert)
			Log.d("onClickedAlertItem","${item.alert.value}")
		}
		binding.executePendingBindings()
	}
}