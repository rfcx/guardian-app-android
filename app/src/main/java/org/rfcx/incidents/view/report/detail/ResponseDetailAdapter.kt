package org.rfcx.incidents.view.report.detail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.databinding.ItemResponseDetailBinding

class ResponseDetailAdapter : RecyclerView.Adapter<ResponseDetailAdapter.ResponseDetailViewHolder>() {
    var items: List<AnswerItem> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ResponseDetailViewHolder(binding: ItemResponseDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        private val msgTextView = binding.msgTextView
        private val iconImageView = binding.iconImageView

        fun bind(item: AnswerItem) {
            msgTextView.text = item.text
            iconImageView.setColorFilter(itemView.resources.getColor(item.color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseDetailViewHolder {
        val binding = ItemResponseDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResponseDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResponseDetailViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
