package android.rfcx.org.ranger.adapter

import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.Message
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_message.view.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageAdapter : RecyclerView.Adapter<MessageViewHolder>() {

    private var messages: MutableList<Message> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val itemView = inflater.inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(itemView)
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

}

class MessageViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    fun bind(message: Message) {
        itemView.itemMessageFromTextView.text = message.from.firstname
        itemView.itemMessageTextView.text = message.text
        itemView.itemMessageLocationTextView.text = message.coords.lat.toString()
    }
}