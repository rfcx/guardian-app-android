package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemAlertSetGuardianGroupBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class AlertSetGuardianGroupView(private val binding: ItemAlertSetGuardianGroupBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.AlertSetGuardianGroupItem) {
		binding.setGuardianGroupItem = item
		binding.onClickedItem = View.OnClickListener {
			listener?.onClickedSetGuardianGroup()
		}
		
		binding.executePendingBindings()
	}
}