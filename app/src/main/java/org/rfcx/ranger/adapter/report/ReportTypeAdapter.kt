package org.rfcx.ranger.adapter.report

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_report_type.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.entity.event.Event

class ReportTypeAdapter : RecyclerView.Adapter<ReportTypeViewHolder>(), OnMessageItemClickListener {
	
	private val source = ArrayList<ReportTypeItem>()
	private var isDisable = false
	var selectedItem: Int = -1
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var onMessageItemClickListener: OnMessageItemClickListener? = null
	
	init {
		source.add(ReportTypeItem(Event.vehicle, R.drawable.ic_truck))
		source.add(ReportTypeItem(Event.trespasser, R.drawable.ic_people))
		source.add(ReportTypeItem(Event.chainsaw, R.drawable.ic_chainsaw))
		source.add(ReportTypeItem(Event.gunshot, R.drawable.ic_gun))
		source.add(ReportTypeItem(Event.other, R.drawable.ic_place_report))
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportTypeViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val itemView = inflater.inflate(R.layout.item_report_type, parent, false)
		return ReportTypeViewHolder(itemView, this@ReportTypeAdapter)
	}
	
	override fun getItemCount(): Int = source.count()
	
	
	override fun onBindViewHolder(holder: ReportTypeViewHolder, position: Int) {
		holder.bind(source[position], selectedItem == position)
	}
	
	override fun onMessageItemClick(position: Int) {
		if (isDisable) return
		selectedItem = position
		notifyDataSetChanged()
		onMessageItemClickListener?.onMessageItemClick(position)
	}
	
	fun getSelectedItem(): ReportTypeItem? {
		if (selectedItem == -1) return null
		return source[selectedItem]
	}
	
	fun disable() {
		onMessageItemClickListener = null
		isDisable = true
	}
	
}

class ReportTypeViewHolder(itemView: View, private val onMessageItemClickListener: OnMessageItemClickListener) : RecyclerView.ViewHolder(itemView) {
	
	fun bind(item: ReportTypeItem, isSelected: Boolean) {
		itemView.reportTypeImageView.setImageResource(item.iconRes)
		if (isSelected) {
			ImageViewCompat.setImageTintList(itemView.reportTypeImageView,
					ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.colorPrimary)))
		} else {
			ImageViewCompat.setImageTintList(itemView.reportTypeImageView, null)
		}
		itemView.setOnClickListener {
			onMessageItemClickListener.onMessageItemClick(adapterPosition)
		}
	}
}