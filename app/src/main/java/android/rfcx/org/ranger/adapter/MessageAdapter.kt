package android.rfcx.org.ranger.adapter

import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.adapter.entity.BaseItem
import android.rfcx.org.ranger.adapter.entity.EventItem
import android.rfcx.org.ranger.adapter.entity.MessageItem
import android.rfcx.org.ranger.adapter.view.EventItemViewHolder
import android.rfcx.org.ranger.adapter.view.MessageViewHolder
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import java.util.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageAdapter(private var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	private var items: MutableList<BaseItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder.itemViewType) {
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

