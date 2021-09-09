package org.rfcx.ranger.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.setFormatLabel

class GuardianItemAdapter(private val onClickListener: (GuardianModel) -> Unit) : RecyclerView.Adapter<GuardianItemAdapter.GuardianItemViewHolder>() {
	var items: List<GuardianModel> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianItemViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian, parent, false)
		return GuardianItemViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GuardianItemViewHolder, position: Int) {
		holder.bind(items[position])
		holder.itemView.setOnClickListener {
			onClickListener(items[position])
		}
	}
	
	inner class GuardianItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val numberImageView = itemView.numberOfAlertsImageView
		private val numberOfAlerts = itemView.numberOfAlertsTextView
		private val guardianName = itemView.guardianNameTextView
		private val distance = itemView.distanceTextView
		
		fun bind(item: GuardianModel) {
			guardianName.text = item.name
			distance.text = item.distance.setFormatLabel()
			numberOfAlerts.text = item.numberOfAlerts.toString()
		}
	}
}

data class GuardianModel(var name: String, var numberOfAlerts: Int, val distance: Float) //TODO:: Change to real model
