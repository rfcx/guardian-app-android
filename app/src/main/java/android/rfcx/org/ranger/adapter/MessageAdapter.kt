package android.rfcx.org.ranger.adapter

import android.graphics.Typeface
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.entity.BaseItem
import android.rfcx.org.ranger.adapter.entity.EventItem
import android.rfcx.org.ranger.adapter.entity.MessageItem
import android.rfcx.org.ranger.adapter.view.EventItemViewHolder
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.RealmHelper
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_message.view.*
import java.util.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private var onMessageItemClickListener: OnMessageItemClickListener

    constructor(onMessageItemClickListener: OnMessageItemClickListener) {
        this.onMessageItemClickListener = onMessageItemClickListener
    }

    private var items: MutableList<BaseItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        return when (viewType) {

            BaseItem.ITEM_EVENT_TYPE -> {
                val itemView = inflater.inflate(R.layout.item_event, parent, false)
                EventItemViewHolder(itemView, onMessageItemClickListener)
            }

            else -> {
                val itemView = inflater.inflate(R.layout.item_message, parent, false)
                MessageViewHolder(itemView, onMessageItemClickListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].itemType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val item = items[position]

        when (holder?.itemViewType) {
            BaseItem.ITEM_MESSAGE_TYPE -> {
                (holder as MessageViewHolder).bind((item as MessageItem).message)
            }

            BaseItem.ITEM_EVENT_TYPE -> {
                (holder as EventItemViewHolder).bind((item as EventItem).event)

            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateMessages(items: List<BaseItem>?) {
        this.items.clear()

        // sort by date
        Collections.sort(items) { item1, item2 -> item2.date.time.compareTo(item1.date.time) }

        if (items != null) {
            this.items.addAll(items)
            notifyDataSetChanged()
        }
    }

    fun getItemAt(position: Int): BaseItem? {
        if (position > items.size || position < 0) {
            return null
        }

        return (items[position])
    }

}

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

interface OnMessageItemClickListener {
    fun onMessageItemClick(position: Int)
}