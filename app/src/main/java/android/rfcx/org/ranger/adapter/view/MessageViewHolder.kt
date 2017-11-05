package android.rfcx.org.ranger.adapter.view

import android.graphics.Typeface
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.OnMessageItemClickListener

import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.RealmHelper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_message.view.*

/**
 * Created by Jingjoeh on 11/5/2017 AD.
 */
class MessageViewHolder(itemView: View?, private var onMessageItemClickListener: OnMessageItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

    fun bind(message: Message) {
        val isOpened = RealmHelper.getInstance().isOpenedMessage(message)
        itemView.itemMessageIconImageView.setColorFilter(ContextCompat.getColor(itemView.context,
                if (isOpened) R.color.divider else R.color.colorAccent))
        itemView.itemMessageFromTextView.typeface = if (isOpened) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
        itemView.itemMessageFromTextView.text = message.from?.firstname
        itemView.itemMessageTextView.text = message.text
        val latLon: String = StringBuilder(message.coords?.lat.toString())
                .append(",")
                .append(message.coords?.lon.toString()).toString()
        itemView.itemMessageLocationTextView.text = latLon
        itemView.itemTimeTextView.text = DateHelper.getEventDate(message.time)

        itemView.setOnClickListener {
            onMessageItemClickListener.onMessageItemClick(adapterPosition)
        }

    }
}