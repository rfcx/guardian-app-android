package org.rfcx.ranger.adapter

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.adapter.entity.EventItem
import org.rfcx.ranger.adapter.entity.MessageItem
import org.rfcx.ranger.adapter.view.EventItemViewHolder
import org.rfcx.ranger.adapter.view.MessageViewHolder
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.rfcx.ranger.adapter.view.ProfileViewHolder
import java.util.*

class MessageAdapter(private val context: Context, private var onMessageItemClickListener: OnMessageItemClickListener,
                     private var onLocationTrackingChangeListener: OnLocationTrackingChangeListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	private var items: MutableList<BaseItem> = ArrayList()
    private var headerInformation: HeaderInformation? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {

            0 -> {
                val itemView = inflater.inflate(R.layout.header_profile, parent, false)
                ProfileViewHolder(itemView, onLocationTrackingChangeListener)
            }

            BaseItem.ITEM_EVENT_TYPE -> {
                val itemView = inflater.inflate(R.layout.item_event, parent, false)
                EventItemViewHolder(itemView, onMessageItemClickListener)
            }

            BaseItem.ITEM_MESSAGE_TYPE -> {
                val itemView = inflater.inflate(R.layout.item_message, parent, false)
                MessageViewHolder(itemView, onMessageItemClickListener)
            }

            else -> {
                throw Exception("Invalid viewType")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else items[position-1].itemType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            val info = headerInformation
            if (info != null) {
                (holder as ProfileViewHolder).bind(context, info.nickname, info.location, info.isLocationTracking)
            }
            return
        }

        val item = items[position-1]

        when (holder.itemViewType) {
            BaseItem.ITEM_MESSAGE_TYPE -> {
                (holder as MessageViewHolder).bind((item as MessageItem).message)
            }

            BaseItem.ITEM_EVENT_TYPE -> {
                (holder as EventItemViewHolder).bind((item as EventItem).event)
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1

    fun getItemAt(position: Int): BaseItem? {
        if (position > items.size || position < 1) {
            return null
        }
        return (items[position-1])
    }

    fun updateMessages(items: List<BaseItem>?) {
        this.items.clear()

        // sort by date
        Collections.sort(items) { item1, item2 -> item2.date.time.compareTo(item1.date.time) }

        if (items != null) {
            this.items.addAll(items)
            notifyDataSetChanged()
        }
    }

    fun updateHeader(nickname: String, location: String, isLocationTracking: Boolean) {
        this.headerInformation = HeaderInformation(nickname, location, isLocationTracking)
        notifyItemChanged(0)
    }

    data class HeaderInformation(val nickname: String, val location: String, val isLocationTracking: Boolean)
}

