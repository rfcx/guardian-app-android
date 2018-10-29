package org.rfcx.ranger.adapter.view

import android.graphics.Typeface
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.EventIcon
import org.rfcx.ranger.util.RealmHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_event.view.*

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

class EventItemViewHolder(itemView: View, private var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {
    fun bind(event: Event) {

        val isOpened = RealmHelper.getInstance().isOpenedEvent(event)
        val isConfirm = RealmHelper.getInstance().isConfirmedEvent(event)

        itemView.tvEventSite.typeface = if (isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
        itemView.tvEventValue.text = event.value
        itemView.tvEventSite.text = event.guardianShortname
        itemView.tvEventDate.text = DateHelper.getEventDate(event?.beginsAt)
        itemView.tvEventTime.text = DateHelper.getEventTime(event)

        itemView.ivEventIcon.setImageResource(EventIcon(event).resId(isOpened))
        itemView.ivEventConfirm.visibility = if (isConfirm) View.VISIBLE else View.INVISIBLE

        // create text latLng
        val latLng: String = StringBuilder(event.latitude.toString())
                .append(", ")
                .append(event.longitude.toString()).toString()

//        itemView.tvEventLocation.text = latLng

        itemView.setOnClickListener {
            onMessageItemClickListener.onMessageItemClick(adapterPosition)
        }
    }
}