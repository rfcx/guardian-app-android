package org.rfcx.ranger.adapter.view

import android.graphics.Typeface
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener

import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.RealmHelper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_message.view.*

class MessageViewHolder(itemView: View, private var onMessageItemClickListener: OnMessageItemClickListener) :
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