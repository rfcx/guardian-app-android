package org.rfcx.ranger.view.report

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_empty_holder.view.*
import kotlinx.android.synthetic.main.item_report_list.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.DateHelper

class ReportListAdapter : ListAdapter<ReportItemBase, RecyclerView.ViewHolder>(ReportListDiffUtil()) {

	companion object {
		private const val reportViewType = 1
		private const val emptyViewType = 2
	}
	
	var onItemClick: ((Int) -> Unit?)? = null

	private val items = arrayListOf<ReportItemBase>()
	private var reportEmptyItem: ReportEmptyItem? = null
	
	fun setEmptyView(@StringRes messageRes: Int, @DrawableRes icon: Int?) {
		reportEmptyItem = ReportEmptyItem(messageRes, icon)
	}
	
	fun setReports(reports: List<Report>) {
		Log.d("setReports", "${reports.count()}")
		if (reportEmptyItem == null)
			throw java.lang.IllegalStateException("Please initial empty view, by using setEmptyView(@StringRes messageRes: Int, @DrawableRes icon: Int?)")
		if (reports.isEmpty()) items.add(reportEmptyItem!!)
		
		items.addAll(reports.map {
			ReportItem(it)
		})
		submitList(items)
	}

	fun getItemAt(position: Int): ReportItemBase? {
		if (position > items.size || position < 1) {
			return null
		}
		return (items[position - 1])
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			reportViewType -> ReportViewHolder(layoutInflater.inflate(R.layout.item_report_list,
					parent, false), onItemClick)
			
			emptyViewType -> ReportEmptyView(layoutInflater.inflate(R.layout.item_empty_holder, parent, false))
			else -> throw IllegalStateException("View type $viewType not found on ReportListAdapter.")
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is ReportEmptyView -> holder.bind(reportEmptyItem!!.messageRes, reportEmptyItem!!.iconRes)
			is ReportViewHolder -> holder.bind((getItem(position) as ReportItem).report)
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		val item = getItem(position)
		return when (item) {
			is ReportItem -> reportViewType
			is ReportEmptyItem -> emptyViewType
			else -> throw IllegalStateException("Item type (${item::class.java.simpleName} not found on ReportListAdapter.")
		}
	}
	
}


class ReportListDiffUtil : DiffUtil.ItemCallback<ReportItemBase>() {
	override fun areItemsTheSame(oldItem: ReportItemBase, newItem: ReportItemBase): Boolean {
		return oldItem.getId() == newItem.getId()
	}
	
	override fun areContentsTheSame(oldItem: ReportItemBase, newItem: ReportItemBase): Boolean {
		return false
	}
	
}

interface ReportItemBase {
	fun getId(): Int
}

data class ReportItem(val report: Report) : ReportItemBase {
	override fun getId(): Int = report.id
}

data class ReportEmptyItem(@StringRes val messageRes: Int, @DrawableRes val iconRes: Int?) : ReportItemBase {
	override fun getId(): Int = -44
}

class ReportViewHolder(itemView: View, private val onItemClick: ((Int) -> Unit?)?) :
		RecyclerView.ViewHolder(itemView) {
	
	init {
		itemView.setOnClickListener {
			onItemClick?.invoke(adapterPosition)
		}
	}
	
	@SuppressLint("SetTextI18n")
	fun bind(report: Report) {
		itemView.reportTypeNameTextView.text = report.value
		val latLon = StringBuilder(report.latitude.toString())
				.append(",")
				.append(report.longitude)
		itemView.reportLocationTextView.text = latLon
		itemView.messageTimeTextView.text = "・${DateHelper.parse(report.reportedAt, "dd MMM yyyy")}"
		itemView.syncedTextView.setTextColor(
				ContextCompat.getColor(itemView.context,
						if (report.syncState == ReportDb.SENT) android.R.color.holo_green_light else android.R.color.holo_orange_light))
		
	}
}

class ReportEmptyView(itemView: View) : RecyclerView.ViewHolder(itemView) {
	
	fun bind(@StringRes messageRes: Int, @DrawableRes icon: Int?) {
		if (icon != null) {
			itemView.iv_empty.setImageResource(icon)
		}
		itemView.tv_empty_list.setText(messageRes)
	}
}
