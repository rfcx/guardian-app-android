package org.rfcx.ranger.adapter.view

import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_message.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.util.DateHelper

class MessageViewHolder(itemView: View, private var onMessageItemClickListener: OnMessageItemClickListener) :
		RecyclerView.ViewHolder(itemView) {
	
	fun bind(message: Message) {
		
		itemView.itemMessageIconImageView.setColorFilter(
				ContextCompat.getColor(itemView.context, if (message.isOpened) R.color.grey_default else R.color.grey_active))
		
		itemView.messageTextView.typeface = if (message.isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
		itemView.messageTextView.text = message.text
		val latLon: String = StringBuilder(message.coords?.lat.toString())
				.append(",")
				.append(message.coords?.lon.toString()).toString()
		itemView.itemMessageLocationTextView.text = latLon
		itemView.messageTimeTextView.text = DateHelper.getMessageDateTime(message.time)
		
		itemView.setOnClickListener {
			onMessageItemClickListener.onMessageItemClick(adapterPosition)
		}
		
	}
}