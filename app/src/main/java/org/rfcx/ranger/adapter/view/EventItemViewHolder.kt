package org.rfcx.ranger.adapter.view

import android.content.Context
import android.graphics.Typeface
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.EventIcon
import org.rfcx.ranger.util.RealmHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_event.view.*
import org.rfcx.ranger.R

class EventItemViewHolder(itemView: View, private var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {

    fun bind(context: Context, event: Event) {

        val isOpened = RealmHelper.getInstance().isOpenedEvent(event)
        val isConfirm = RealmHelper.getInstance().isConfirmedEvent(event)

        itemView.tvEventValue.typeface = if (isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
        itemView.tvEventSite.setTextColor(context.getResources().getColor(if (isOpened) R.color.text_secondary else R.color.text_primary))

        itemView.tvEventValue.text = event.value
        itemView.tvEventSite.text = event.guardianShortname
        itemView.tvEventDate.text = DateHelper.getEventTime(event)

        itemView.ivEventIcon.setImageResource(EventIcon(event).resId(isOpened))
        itemView.ivEventConfirm.visibility = if (isConfirm) View.VISIBLE else View.INVISIBLE

//        val latLng: String = StringBuilder(event.latitude.toString())
//                .append(", ")
//                .append(event.longitude.toString()).toString()
//        itemView.tvEventLocation.text = latLng

        itemView.setOnClickListener {
            onMessageItemClickListener.onMessageItemClick(adapterPosition)
        }
    }
}