package org.rfcx.ranger.adapter.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_event.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.EventIcon

class EventItemViewHolder(itemView: View, private var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {
	
	@SuppressLint("SetTextI18n")
	fun bind(context: Context, event: Event) {
		itemView.tvEventValue.typeface = if (event.isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
		itemView.tvEventSite.setTextColor(
				ContextCompat.getColor(context, (if (event.isOpened) R.color.text_secondary else R.color.text_primary)))
		
		itemView.tvEventValue.text = event.value
		itemView.tvEventSite.text = event.guardianShortname
		itemView.tvEventDate.text = "・${DateHelper.getEventTime(event)}"
		
		itemView.ivEventIcon.setImageResource(EventIcon(event).resId(event.isOpened))
		itemView.ivEventConfirm.visibility = if (event.isConfirmed) View.VISIBLE else View.INVISIBLE

//        val latLng: String = StringBuilder(event.latitude.toString())
//                .append(", ")
//                .append(event.longitude.toString()).toString()
//        itemView.tvEventLocation.text = latLng
		
		itemView.setOnClickListener {
			onMessageItemClickListener.onMessageItemClick(adapterPosition)
		}
	}
}