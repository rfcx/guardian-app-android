package org.rfcx.ranger.view.report.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_response_detail.view.*
import org.rfcx.ranger.R

class ResponseDetailAdapter : RecyclerView.Adapter<ResponseDetailAdapter.ResponseDetailViewHolder>() {
	var items: List<AnswerItem> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	inner class ResponseDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val msgTextView = itemView.msgTextView
		private val iconImageView = itemView.iconImageView
		
		fun bind(item: AnswerItem) {
			msgTextView.text = item.text
			iconImageView.setColorFilter(itemView.resources.getColor(item.color))
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseDetailViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_response_detail, parent, false)
		return ResponseDetailViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ResponseDetailViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	override fun getItemCount(): Int = items.size
}
