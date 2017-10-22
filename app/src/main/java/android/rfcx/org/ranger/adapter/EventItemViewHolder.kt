package android.rfcx.org.ranger.adapter

import android.rfcx.org.ranger.entity.Event
import android.rfcx.org.ranger.util.DateHelper
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_event.view.*

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

class EventItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun bind(event: Event) {
        itemView.tvEventValue.text = event.value
        itemView.tvEventSite.text = event.site
        itemView.tvEventTime.text = String.format("%s - %s", DateHelper.getMessageDateTime(event.beginsAt),
                DateHelper.getMessageDateTime(event.endAt))

        // create text latLng
        val latLng: String = StringBuilder(event.latitude.toString())
                .append(",")
                .append(event.longitude.toString()).toString()

        itemView.tvEventLocation.text = latLng
    }
}