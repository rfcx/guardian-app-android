package org.rfcx.ranger.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_draft_reports.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.project.isGuest
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.SyncState
import org.rfcx.ranger.util.setDrawableImage
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo

class DraftReportsAdapter(private val listener: ReportOnClickListener) : RecyclerView.Adapter<DraftReportsAdapter.ReportsViewHolder>() {
	var items: List<Response> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftReportsAdapter.ReportsViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_draft_reports, parent, false)
		return ReportsViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: DraftReportsAdapter.ReportsViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	override fun getItemCount(): Int = items.size
	
	inner class ReportsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		
		private val guardianName = itemView.guardianNameTextView
		private val dateTextView = itemView.dateTextView
		private val actionImageView = itemView.actionImageView
		
		fun bind(report: Response) {
			actionImageView.setDrawableImage(itemView.context, R.drawable.ic_delete_outline)
			setClickable(itemView, report.syncState == SyncState.SENT.value)
			
			if (report.syncState == SyncState.SENT.value) {
				guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
				dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
			} else {
				guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
				dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
			}
			
			guardianName.text = report.streamName
			dateTextView.text = report.investigatedAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			
			actionImageView.setOnClickListener {
				listener.onClickedDelete(report)
			}
			
			itemView.setOnClickListener {
				listener.onClickedItem(report)
			}
		}
	}
	
	fun setClickable(view: View?, clickable: Boolean) {
		if (view != null) {
			if (view is ViewGroup) {
				val viewGroup = view
				for (i in 0 until viewGroup.childCount) {
					setClickable(viewGroup.getChildAt(i), clickable)
				}
			}
			view.isClickable = clickable
		}
	}
}

interface ReportOnClickListener {
	fun onClickedDelete(response: Response)
	fun onClickedItem(response: Response)
}
