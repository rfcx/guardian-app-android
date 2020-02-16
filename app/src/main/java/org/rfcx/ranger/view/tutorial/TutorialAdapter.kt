package org.rfcx.ranger.view.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R

class TutorialAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	var items: List<Int> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			TUTORIAL_EXPLAIN_ALERT -> {
				val itemView = inflater.inflate(R.layout.fragment_slider_one, parent, false)
				AlertTutorialView(itemView)
			}
			TUTORIAL_EXPLAIN_ALERT_BOTTOM_DIALOG -> {
				val itemView = inflater.inflate(R.layout.fragment_slider_two, parent, false)
				AlertBottomDialogTutorialView(itemView)
			}
			TUTORIAL_EXPLAIN_ENABLE_TRACKING -> {
				val itemView = inflater.inflate(R.layout.fragment_slider_three, parent, false)
				EnableTrackingTutorialView(itemView)
			}
			TUTORIAL_EXPLAIN_CREATE_REPORTS -> {
				val itemView = inflater.inflate(R.layout.fragment_slider_four, parent, false)
				CreateReportsTutorialView(itemView)
			}
			else -> {
				throw Exception("Invalid viewType")
			}
		}
	}
	
	override fun getItemCount(): Int {
		return items.count()
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (val item = items[position]) {
			TUTORIAL_EXPLAIN_ALERT -> TUTORIAL_EXPLAIN_ALERT
			TUTORIAL_EXPLAIN_ALERT_BOTTOM_DIALOG -> TUTORIAL_EXPLAIN_ALERT_BOTTOM_DIALOG
			TUTORIAL_EXPLAIN_ENABLE_TRACKING -> TUTORIAL_EXPLAIN_ENABLE_TRACKING
			TUTORIAL_EXPLAIN_CREATE_REPORTS -> TUTORIAL_EXPLAIN_CREATE_REPORTS
			else -> throw IllegalStateException("Item type (${item::class.java.simpleName} not found on TutorialAdapter.")
		}
	}
	
	companion object {
		const val TUTORIAL_EXPLAIN_ALERT = 1
		const val TUTORIAL_EXPLAIN_ALERT_BOTTOM_DIALOG = 2
		const val TUTORIAL_EXPLAIN_ENABLE_TRACKING = 3
		const val TUTORIAL_EXPLAIN_CREATE_REPORTS = 4
	}
}

class AlertTutorialView(itemView: View) : RecyclerView.ViewHolder(itemView)

class AlertBottomDialogTutorialView(itemView: View) : RecyclerView.ViewHolder(itemView)

class EnableTrackingTutorialView(itemView: View) : RecyclerView.ViewHolder(itemView)

class CreateReportsTutorialView(itemView: View) : RecyclerView.ViewHolder(itemView)
