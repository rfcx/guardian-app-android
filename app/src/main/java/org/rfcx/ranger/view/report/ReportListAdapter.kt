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
import org.rfcx.ranger.entity.event.Event
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
	
	fun setReports(reports: List<ReportItem>) {
		Log.d("setReports", "${reports.count()}")
		if (reportEmptyItem == null)
			throw java.lang.IllegalStateException("Please initial empty view, by using setEmptyView(@StringRes messageRes: Int, @DrawableRes icon: Int?)")
		items.clear()
		items.addAll(reports)
		if (reports.isEmpty()) items.add(reportEmptyItem!!)
		submitList(ArrayList(items))
	}
	
	fun getItemAt(position: Int): ReportItemBase? {
		return items[position]
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
			is ReportViewHolder -> holder.bind((getItem(position) as ReportItem))
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (val item = getItem(position)) {
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
		return if (oldItem is ReportItem && newItem is ReportItem) {
			oldItem.getId() == newItem.getId() && oldItem.report.syncState == newItem.report.syncState
					&& oldItem.attachImagesCount == newItem.attachImagesCount
					&& oldItem.attachImagesUnSyncCount == newItem.attachImagesUnSyncCount
		} else {
			false
		}
	}
}

interface ReportItemBase {
	fun getId(): Int
}

data class ReportItem(val report: Report, val attachImagesCount: Int, val attachImagesUnSyncCount: Int) : ReportItemBase {
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
	fun bind(reportItem: ReportItem) {
		Log.d("ReportListAdapter", "${reportItem.report.id} -- ${reportItem.report.syncState}")
		val report = reportItem.report
		itemView.reportTypeNameTextView.text = report.value
		val latLon = StringBuilder(report.latitude.toString())
				.append(",")
				.append(report.longitude)
		itemView.reportLocationTextView.text = latLon
		itemView.messageTimeTextView.text = "・${DateHelper.formatShortDate(report.reportedAt)}"
		itemView.syncedTextView.setTextColor(
				ContextCompat.getColor(itemView.context,
						if (report.syncState == ReportDb.SENT) android.R.color.holo_green_light else android.R.color.holo_orange_light))
		
		itemView.reportTypeImageView.setImageResource(
				when (report.value) {
					Event.vehicle -> R.drawable.ic_truck
					Event.trespasser -> R.drawable.ic_people
					Event.chainsaw -> R.drawable.ic_chainsaw
					Event.gunshot -> R.drawable.ic_gun
					else -> R.drawable.ic_pin_huge
				}
		)
		if (reportItem.attachImagesCount == 0) {
			itemView.imageSyncState.visibility = View.GONE
		} else {
			if (reportItem.attachImagesUnSyncCount == 0) {
				itemView.imageSyncState.text = itemView.context.getString(
						R.string.images_sync_format, reportItem.attachImagesCount)
			} else {
				itemView.imageSyncState.text = itemView.context.getString(
						R.string.images_unsync_format, reportItem.attachImagesCount, reportItem.attachImagesUnSyncCount)
			}
			
			itemView.imageSyncState.visibility = View.VISIBLE
		}
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

