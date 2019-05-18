package org.rfcx.ranger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_report_image.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.GlideApp

class ReportImageAdapter : RecyclerView.Adapter<ReportImageAdapter.ReportImageAdapterViewHolder>() {
	var images = listOf<String>()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportImageAdapterViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_report_image, parent, false)
		return ReportImageAdapterViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ReportImageAdapterViewHolder, position: Int) {
		holder.bind(images[position])
	}
	
	override fun getItemCount(): Int = images.size
	
	inner class ReportImageAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		
		fun bind(imagePath: String) {
			GlideApp.with(itemView.imageReport)
					.load(imagePath)
					.into(itemView.imageReport)
		}
	}
}