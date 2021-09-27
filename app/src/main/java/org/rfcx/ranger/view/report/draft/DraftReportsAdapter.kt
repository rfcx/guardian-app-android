package org.rfcx.ranger.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_draft_reports.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.util.setDrawableImage
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo
import java.util.*

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
			guardianName.text = report.guardianName
			dateTextView.text = report.investigatedAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			
			actionImageView.setOnClickListener {
				listener.onClickedDelete(report)
			}
		}
	}
}

interface ReportOnClickListener {
	fun onClickedDelete(response: Response)
}
