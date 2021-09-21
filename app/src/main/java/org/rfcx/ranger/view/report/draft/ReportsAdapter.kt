package org.rfcx.ranger.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_reports.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.util.setDrawableImage
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo
import java.util.*

class ReportsAdapter(private val listener: ReportOnClickListener) :
		RecyclerView.Adapter<ReportsAdapter.ReportsViewHolder>() {
	var screen: Screen? = null
	var items: List<ReportModel> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportsAdapter.ReportsViewHolder {
		val view =
				LayoutInflater.from(parent.context).inflate(R.layout.item_reports, parent, false)
		return ReportsViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ReportsAdapter.ReportsViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	override fun getItemCount(): Int = items.size
	
	inner class ReportsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val guardianName = itemView.guardianNameTextView
		private val dateTextView = itemView.dateTextView
		private val reportIdTextView = itemView.reportIdTextView
		private val actionImageView = itemView.actionImageView
		
		fun bind(report: ReportModel) {
			if (screen == Screen.SUBMITTED_REPORTS) {
				actionImageView.setDrawableImage(itemView.context, report.syncImage)
				reportIdTextView.visibility = if (report.reportId != null) View.VISIBLE else View.GONE
				reportIdTextView.text = itemView.context.getString(R.string.report_id, report.reportId)
			} else {
				reportIdTextView.visibility = View.GONE
				actionImageView.setDrawableImage(itemView.context, R.drawable.ic_delete_outline)
			}
			guardianName.text = report.nameGuardian
			dateTextView.text = report.date.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			
			actionImageView.setOnClickListener {
				if (screen == Screen.DRAFT_REPORTS) {
					listener.onClickedDelete(report)
				}
			}
		}
	}
}

data class ReportModel(var nameGuardian: String, var date: Date, val syncInfo: Int, val reportId: String? = null) {
	val syncImage = when (syncInfo) { // 0 unsent, 1 uploading, 2 uploaded
		0 -> R.drawable.ic_cloud_queue
		1 -> R.drawable.ic_cloud_upload
		else -> R.drawable.ic_cloud_done
	}
} //TODO:: Change to real model

interface ReportOnClickListener {
	fun onClickedDelete(report: ReportModel)
}
