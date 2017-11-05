package android.rfcx.org.ranger.adapter.view

import android.graphics.Typeface
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.OnMessageItemClickListener
import android.rfcx.org.ranger.entity.event.Event
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.RealmHelper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_event.view.*

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

class EventItemViewHolder(itemView: View?, private var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {
    fun bind(event: Event) {

        val isOpened = RealmHelper.getInstance().isOpenedEvent(event)
        val isConfirm = RealmHelper.getInstance().isConfirmedEvent(event)
        itemView.ivEventIcon.setColorFilter(ContextCompat.getColor(itemView.context,
                if (isOpened) R.color.divider else R.color.orange))
        itemView.tvEventSite.typeface = if (isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
        itemView.tvEventValue.text = event.value
        itemView.tvEventSite.text = event.site
        itemView.tvEventDate.text = DateHelper.getEventDate(event.beginsAt)
        itemView.tvEventTime.text = String.format("%s - %s", DateHelper.getEventTime(event.beginsAt),
                DateHelper.getEventTime(event.endAt))

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