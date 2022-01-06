package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue
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
		private val noneTextView = itemView.noneTextView
		private val incidentIdTextView = itemView.incidentIdTextView
		private val otherLayout = itemView.otherLayout
		private val numOfOtherTextView = itemView.numOfOtherTextView
		private val guardianNameTextView = itemView.guardianNameTextView
		
		fun bind(item: StreamItem) {
			guardianName.text = item.streamName
			val alerts = item.alerts.sortedBy { a -> a.start }
			recentTextView.visibility = if (alerts.isNotEmpty() && System.currentTimeMillis() - alerts.last().start.time <= 6 * HOUR) View.VISIBLE else View.GONE
			hotTextView.visibility = if (alerts.size > 10) View.VISIBLE else View.GONE
			
			val typeOfAlert = alerts.distinctBy { a -> a.classification?.value }
			
			if (typeOfAlert.isNotEmpty()) {
				var number = 0
				typeOfAlert.forEachIndexed { index, alert ->
					val type = alert.classification?.value
					type?.let {
						if (index < 2) {
							showIconType(it, itemView, alerts)
						} else {
							otherLayout.visibility = View.VISIBLE
							number += getNumberOfAlertByType(alerts, type).toInt()
							numOfOtherTextView.text = (number).toString()
						}
					}
				}
			}
			
			
			if (item.eventSize == 0) {
				timeTextView.visibility = View.GONE
				bellImageView.visibility = View.GONE
				noneTextView.visibility = View.VISIBLE
				incidentIdTextView.visibility = View.GONE
				guardianNameTextView.setPadding(16.toPx, 16.toPx, 16.toPx, 16.toPx)
			} else {
				timeTextView.visibility = View.VISIBLE
				bellImageView.visibility = View.VISIBLE
				timeTextView.text = item.eventTime
				noneTextView.visibility = View.GONE
				incidentIdTextView.visibility = View.VISIBLE
				guardianNameTextView.setPadding(16.toPx, 16.toPx, 0.toPx, 10.toPx)
			}
		}
	}
	
	fun showIconType(type: String, itemView: View, alerts: List<Alert>) {
		val chainsawLayout = itemView.chainsawLayout
		val peopleLayout = itemView.peopleLayout
		val gunLayout = itemView.gunLayout
		val numOfChainsawTextView = itemView.numOfChainsawTextView
		val numOfPeopleTextView = itemView.numOfPeopleTextView
		val numOfGunTextView = itemView.numOfGunTextView
		
		when (type) {
			GUNSHOT -> {
				gunLayout.visibility = View.VISIBLE
				numOfGunTextView.text = getNumberOfAlertByType(alerts, type)
			}
			HUMAN_VOICE -> {
				peopleLayout.visibility = View.VISIBLE
				numOfPeopleTextView.text = getNumberOfAlertByType(alerts, type)
			}
			CHAINSAW -> {
				chainsawLayout.visibility = View.VISIBLE
				numOfChainsawTextView.text = getNumberOfAlertByType(alerts, type)
			}
		}
	}
	
	private fun getNumberOfAlertByType(alerts: List<Alert>, type: String): String {
		return alerts.filter { a -> a.classification?.value == type }.size.toString()
	}
	
	val Number.toPx
		get() = TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				this.toFloat(),
				Resources.getSystem().displayMetrics).toInt()
	
	companion object {
		private const val MINUTE = 60L * 1000L
		private const val HOUR = 60L * MINUTE
		private const val DAY = 24L * HOUR
		private const val GUNSHOT = "gunshot"
		private const val HUMAN_VOICE = "humanvoice"
		private const val CHAINSAW = "chainsaw"
	}
}

data class StreamItem(val eventSize: Int, val distance: Double?, val streamName: String, val streamId: String, val eventTime: String? = null, val alerts: List<Alert>)
