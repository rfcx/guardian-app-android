package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian.view.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.alert.Alert
import java.util.*

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
		
		fun bind(item: StreamItem) {
			guardianName.text = item.streamName
			val alerts = item.alerts.sortedBy { a -> a.start }
			recentTextView.visibility = if (alerts.isNotEmpty() && System.currentTimeMillis() - alerts.last().start.time <= 6 * HOUR) View.VISIBLE else View.GONE
			hotTextView.visibility = if (alerts.size > 10) View.VISIBLE else View.GONE
			
			if (item.eventSize == 0) {
				timeTextView.visibility = View.GONE
				bellImageView.visibility = View.GONE
			} else {
				timeTextView.visibility = View.VISIBLE
				bellImageView.visibility = View.VISIBLE
				timeTextView.text = item.eventTime
			}
		}
		
		private fun setTimeNoResponse(context: Context, eventTime: Date): String {
			val diffTimeNoResponse: Long = Date().time - eventTime.time
			val days = diffTimeNoResponse / DAY
			val hours = ((diffTimeNoResponse - (DAY * days)) / (HOUR))
			val minutes = (diffTimeNoResponse - (DAY * days) - (HOUR * hours)) / MINUTE
			
			val stringBuilder = StringBuilder()
			if (days != 0L) {
				stringBuilder.append(days)
				if (days == 1L) {
					stringBuilder.append(" ${context.getString(R.string.day)} ")
				} else {
					stringBuilder.append(" ${context.getString(R.string.days)} ")
				}
			}
			
			if (hours != 0L) {
				stringBuilder.append(hours)
				if (hours == 1L) {
					stringBuilder.append(" ${context.getString(R.string.hr)} ")
				} else {
					stringBuilder.append(" ${context.getString(R.string.hrs)} ")
				}
			}
			
			if (minutes != 0L) {
				stringBuilder.append(minutes)
				stringBuilder.append(" ${context.getString(R.string.min)} ")
			}
			return "$stringBuilder"
		}
	}
	
	companion object {
		private const val MINUTE = 60L * 1000L
		private const val HOUR = 60L * MINUTE
		private const val DAY = 24L * HOUR
	}
}

data class StreamItem(val eventSize: Int, val distance: Double?, val streamName: String, val streamId: String, val eventTime: String? = null, val alerts: List<Alert>)
