package org.rfcx.ranger.adapter.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemSeeMoreBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class SeeMoreViewHolder(private val binding: ItemSeeMoreBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.SeeMoreItem) {
		binding.seeMoreItem = item
		binding.onClickedItem = View.OnClickListener {
			listener?.onClickedSeeMore()
		}
		
		binding.executePendingBindings()
	}
}