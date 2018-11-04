package org.rfcx.ranger.adapter.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_report_type.view.*
import org.rfcx.ranger.R

class ReportTypeAdapter : RecyclerView.Adapter<ReportTypeViewHolder>() {
	
	private val source = ArrayList<ReportTypeItem>()
	
	init {
		source.add(ReportTypeItem("chainsaw", R.drawable.ic_chainsaw, false))
		source.add(ReportTypeItem("chainsaw", R.drawable.ic_chainsaw, false))
		source.add(ReportTypeItem("chainsaw", R.drawable.ic_chainsaw, false))
		source.add(ReportTypeItem("chainsaw", R.drawable.ic_chainsaw, false))
		source.add(ReportTypeItem("chainsaw", R.drawable.ic_chainsaw, false))
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportTypeViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val itemView = inflater.inflate(R.layout.item_report_type, parent, false)
		return ReportTypeViewHolder(itemView)
	}
	
	override fun getItemCount(): Int = source.count()
	
	
	override fun onBindViewHolder(holder: ReportTypeViewHolder, position: Int) {
		holder.bind(source[position])
	}
}

class ReportTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
	
	fun bind(item: ReportTypeItem) {
		itemView.reportTypeImageView.setImageResource(item.iconRes)
	}
}