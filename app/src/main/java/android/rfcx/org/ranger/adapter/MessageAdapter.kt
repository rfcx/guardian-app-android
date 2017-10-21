package android.rfcx.org.ranger.adapter

import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.util.DateHelper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_message.view.*
import java.util.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageAdapter : RecyclerView.Adapter<MessageViewHolder> {

    private var onMessageItemClickListener: OnMessageItemClickListener

    constructor(onMessageItemClickListener: OnMessageItemClickListener) {
        this.onMessageItemClickListener = onMessageItemClickListener
    }

    private var messages: MutableList<Message> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val itemView = inflater.inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView, onMessageItemClickListener)
    }

    override fun onBindViewHolder(holder: MessageViewHolder?, position: Int) {
        holder?.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(messages: List<Message>) {
        this.messages.clear()
        this.messages.addAll(messages)
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): Message? {
        if (position > messages.size || position < 0) {
            return null
        }

        return messages[position]
    }

}

class MessageViewHolder(itemView: View?, var onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {

    fun bind(message: Message) {
        itemView.itemMessageFromTextView.text = message.from?.firstname
        itemView.itemMessageTextView.text = message.text
        val latLon: String = StringBuilder(message.coords?.lat.toString())
                .append(",")
                .append(message.coords?.lon.toString()).toString()
        itemView.itemMessageLocationTextView.text = latLon
        itemView.itemTimeTextView.text = DateHelper.getMessageDateTime(message.time)

        itemView.setOnClickListener {
            onMessageItemClickListener.onMessageItemClick(layoutPosition)
        }

    }
}

interface OnMessageItemClickListener {
    fun onMessageItemClick(position: Int)
}