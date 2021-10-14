package org.rfcx.ranger.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.setFormatLabel
import java.util.*

class GuardianItemAdapter(private val onClickListener: (EventGroup) -> Unit) : RecyclerView.Adapter<GuardianItemAdapter.GuardianItemViewHolder>() {
	var items: List<EventGroup> = arrayListOf()
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
		
		fun bind(item: EventGroup) {
			val preferences = Preferences.getInstance(itemView.context)
			val time = preferences.getLong(Preferences.LATEST_CURRENT_LOCATION_TIME, 0)
			val diff = Date().time - time
			
			guardianName.text = item.streamName
			distance.visibility = if (item.distance == null || item.distance >= 100000 || diff >= 30 * 60 * 1000) View.GONE else View.VISIBLE
			distance.text = item.distance?.setFormatLabel()
			numberOfAlerts.text = if (item.eventSize > 99) itemView.context.getString(R.string.num_more_then_99) else item.eventSize.toString()
			
			if (item.eventSize == 0) {
				numberImageView.setImageResource(R.drawable.bg_circle_green)
			} else {
				numberImageView.setImageResource(R.drawable.bg_circle_red)
			}
		}
	}
}

data class EventGroup(val eventSize: Int, val distance: Double?, val streamName: String, val streamId: String)
