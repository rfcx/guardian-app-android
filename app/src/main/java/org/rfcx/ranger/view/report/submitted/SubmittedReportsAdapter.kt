package org.rfcx.ranger.view.report.submitted

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_submitted_reports.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.syncImage
import org.rfcx.ranger.entity.response.syncLabel
import org.rfcx.ranger.util.setDrawableImage
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo

class SubmittedReportsAdapter :
		RecyclerView.Adapter<SubmittedReportsAdapter.ReportsViewHolder>() {
	var items: List<Response> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmittedReportsAdapter.ReportsViewHolder {
		val view =
				LayoutInflater.from(parent.context).inflate(R.layout.item_submitted_reports, parent, false)
		return ReportsViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: SubmittedReportsAdapter.ReportsViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	override fun getItemCount(): Int = items.size
	
	inner class ReportsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val guardianName = itemView.guardianNameTextView
		private val dateTextView = itemView.dateTextView
		private val reportIdTextView = itemView.reportIdTextView
		private val syncLabelTextView = itemView.syncLabelTextView
		private val actionImageView = itemView.actionImageView
		
		fun bind(report: Response) {
			actionImageView.setDrawableImage(itemView.context, report.syncImage())
			reportIdTextView.visibility = if (report.guid != null) View.VISIBLE else View.GONE
			reportIdTextView.text = report.guid.toString()
			syncLabelTextView.text = itemView.context.getString(report.syncLabel())
			guardianName.text = report.guardianName
			dateTextView.text = report.investigatedAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
		}
	}
}
