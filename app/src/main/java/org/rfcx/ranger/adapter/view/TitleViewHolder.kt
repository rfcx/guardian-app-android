package org.rfcx.ranger.adapter.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_title_holder.view.*

class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
	
	fun bind(title: String) {
		itemView.titleTextView?.text = title
	}
}