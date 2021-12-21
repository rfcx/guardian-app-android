package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian.view.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.alert.Alert

class StreamItemAdapter(private val onClickListener: (StreamItem) -> Unit) : RecyclerView.Adapter<StreamItemAdapter.GuardianItemViewHolder>() {
	var items: List<StreamItem> = arrayListOf()
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
		private val guardianName = itemView.guardianNameTextView
		private val timeTextView = itemView.timeTextView
		private val bellImageView = itemView.bellImageView
		private val recentTextView = itemView.recentTextView
		private val hotTextView = itemView.hotTextView
		private val verifiedImageView = itemView.verifiedImageView
		private val noneTextView = itemView.noneTextView
		private val incidentIdTextView = itemView.incidentIdTextView
		
		fun bind(item: StreamItem) {
			guardianName.text = item.streamName
			val alerts = item.alerts.sortedBy { a -> a.start }
			recentTextView.visibility = if (alerts.isNotEmpty() && System.currentTimeMillis() - alerts.last().start.time <= 6 * HOUR) View.VISIBLE else View.GONE
			hotTextView.visibility = if (alerts.size > 10) View.VISIBLE else View.GONE
			
			if (item.eventSize == 0) {
				timeTextView.visibility = View.GONE
				bellImageView.visibility = View.GONE
				verifiedImageView.visibility = View.VISIBLE
				noneTextView.visibility = View.VISIBLE
				incidentIdTextView.visibility = View.GONE
			} else {
				timeTextView.visibility = View.VISIBLE
				bellImageView.visibility = View.VISIBLE
				timeTextView.text = item.eventTime
				verifiedImageView.visibility = View.GONE
				noneTextView.visibility = View.GONE
				incidentIdTextView.visibility = View.VISIBLE
			}
		}
	}
	
	companion object {
		private const val MINUTE = 60L * 1000L
		private const val HOUR = 60L * MINUTE
		private const val DAY = 24L * HOUR
	}
}

data class StreamItem(val eventSize: Int, val distance: Double?, val streamName: String, val streamId: String, val eventTime: String? = null, val alerts: List<Alert>)
