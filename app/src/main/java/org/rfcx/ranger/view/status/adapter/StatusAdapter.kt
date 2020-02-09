package org.rfcx.ranger.view.status.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.adapter.entity.TitleItem
import org.rfcx.ranger.adapter.view.SeeMoreViewHolder
import org.rfcx.ranger.adapter.view.TitleViewHolder
import org.rfcx.ranger.databinding.*
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_ALERT
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_ALERT_EMPTY
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_ALERT_LOADING
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_ALERT_SET_GUARDIAN_GROUP
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_PROFILE
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_REPORT_EMPTY
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_REPORT_HISTORY
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_SEE_MORE
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_SYNC_INFO
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_TITLE
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_USER_STATUS
import org.rfcx.ranger.view.status.adapter.viewholder.*

class StatusAdapter(private val statusTitle: String?, private val alertTitle: String?,
                    private val reportTitle: String?, private val seeMoreButton: String?,
                    private val context: Context?)
	: ListAdapter<StatusAdapter.StatusItemBase, RecyclerView.ViewHolder>(StatusListDiffUtil()), SyncingViewCallback {
	
	private var listener: StatusFragmentListener? = null
	
	fun setListener(listener: StatusFragmentListener) {
		this.listener = listener
	}
	
	private var profile: ProfileItem? = null
	private var stat: UserStatusItem? = null
	private var reports: ArrayList<ReportItem>? = arrayListOf()
	private var alerts: ArrayList<AlertItem>? = arrayListOf()
	private var syncInfo: SyncInfoItem? = null
	
	fun updateHeader(header: ProfileItem) {
		profile = header
		update()
	}
	
	fun updateStat(stat: UserStatusItem) {
		this.stat = stat
		update()
	}
	
	fun updateReportList(newLists: List<ReportItem>) {
		if (newLists.isNotEmpty()) {
			reports = arrayListOf()
			reports?.addAll(newLists)
		} else {
			reports = null // display no reports
		}
		update()
	}
	
	fun updateAlertList(newLists: List<AlertItem>) {
		if (newLists.isNotEmpty()) {
			alerts = arrayListOf()
			alerts?.addAll(newLists)
		} else {
			alerts = null
		}
		update()
	}
	
	fun updateSyncInfo(syncInfo: SyncInfo?) {
		this.syncInfo = if (syncInfo != null) SyncInfoItem(syncInfo) else null
		update()
	}
	
	fun update() {
		val newList = arrayListOf<StatusItemBase>()
		profile?.let {
			newList.add(it)
		}
		
		syncInfo?.let {
			newList.add(it)
		}
		
		stat?.let {
			statusTitle?.let {
				newList.add(TitleItem(it))
			}
			newList.add(it)
		}
		
		alertTitle?.let {
			newList.add(TitleItem(it))
		}
		
		if (alerts != null && alerts!!.isEmpty()) {
			when {
				context?.getGuardianGroup() == null -> // not have group
					newList.add(AlertSetGuardianGroupItem())
				context.getGuardianGroup() !== null ->
					newList.add(AlertLoading())
				alerts!!.size == 0 -> // have group but not have alert
					newList.add(AlertEmpty())
			}
		}
		
		if (alerts != null && alerts!!.isNotEmpty()) {
			newList.addAll(alerts!!)
			
			seeMoreButton?.let {
				newList.add(SeeMoreItem(it))
			}
		}
		
		reportTitle?.let {
			newList.add(TitleItem(it))
		}
		
		if (reports == null) {
			newList.add(ReportEmpty())
		}
		
		if (reports != null && reports!!.isNotEmpty()) {
			newList.addAll(reports!!)
		}
		
		submitList(newList)
	}
	
	// region @link{ SyncingViewCallback }
	override fun onUploadCompleted() {
		updateSyncInfo(null) // for hide view syncing
	}
	// endregion
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			ITEM_PROFILE -> {
				val itemView = DataBindingUtil.inflate<ItemHeaderProfileBinding>(inflater, R.layout.item_header_profile, parent, false)
				ProfileView(itemView, listener)
			}
			ITEM_USER_STATUS -> {
				val itemView = DataBindingUtil.inflate<ItemUserStatusBinding>(inflater, R.layout.item_user_status, parent, false)
				UserStatusView(itemView)
			}
			ITEM_ALERT -> {
				val itemView = DataBindingUtil.inflate<ItemStatusAlertBinding>(inflater, R.layout.item_status_alert, parent, false)
				AlertView(itemView, listener)
			}
			ITEM_ALERT_EMPTY -> {
				val itemView = inflater.inflate(R.layout.item_alert_empty, parent, false)
				EmptyAlertView(itemView)
			}
			ITEM_ALERT_LOADING -> {
				val itemView = inflater.inflate(R.layout.item_alert_loading, parent, false)
				AlertLoadingView(itemView)
			}
			ITEM_ALERT_SET_GUARDIAN_GROUP -> {
				val itemView = DataBindingUtil.inflate<ItemAlertSetGuardianGroupBinding>(inflater, R.layout.item_alert_set_guardian_group, parent, false)
				AlertSetGuardianGroupView(itemView, listener)
			}
			ITEM_REPORT_HISTORY -> {
				val itemView = DataBindingUtil.inflate<ItemStatusReportBinding>(inflater, R.layout.item_status_report, parent, false)
				ReportView(itemView, listener)
			}
			ITEM_TITLE -> {
				val itemView = inflater.inflate(R.layout.item_title_holder, parent, false)
				TitleViewHolder(itemView)
			}
			ITEM_SEE_MORE -> {
				val itemView = DataBindingUtil.inflate<ItemSeeMoreBinding>(inflater, R.layout.item_see_more, parent, false)
				SeeMoreViewHolder(itemView, listener)
			}
			ITEM_REPORT_EMPTY -> {
				val itemView = inflater.inflate(R.layout.item_report_empty, parent, false)
				EmptyReportView(itemView)
			}
			ITEM_SYNC_INFO -> {
				val itemView = DataBindingUtil.inflate<ItemStatusSyncingBinding>(inflater, R.layout.item_status_syncing, parent, false)
				SyncingView(itemView, this)
			}
			else -> {
				throw Exception("Invalid viewType")
			}
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = getItem(position)
		when (holder) {
			is ProfileView -> {
				holder.bind(item as ProfileItem)
			}
			is UserStatusView -> {
				holder.bind(item as UserStatusItem)
			}
			is AlertView -> {
				holder.bind(item as AlertItem)
			}
			is SeeMoreViewHolder -> {
				holder.bind(item as SeeMoreItem)
			}
			is ReportView -> {
				holder.bind(item as ReportItem)
			}
			is TitleViewHolder -> {
				holder.bind((item as TitleItem).title)
			}
			is SyncingView -> {
				holder.bind(item as SyncInfoItem)
			}
			is AlertSetGuardianGroupView -> {
				holder.bind(item as AlertSetGuardianGroupItem)
			}
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return getItem(position).getViewType()
	}
	
	class StatusListDiffUtil : DiffUtil.ItemCallback<StatusItemBase>() {
		override fun areItemsTheSame(oldItem: StatusItemBase, newItem: StatusItemBase): Boolean {
			return oldItem.getId() == newItem.getId()
		}
		
		@SuppressLint("DiffUtilEquals")
		override fun areContentsTheSame(oldItem: StatusItemBase, newItem: StatusItemBase): Boolean {
			if (oldItem.getViewType() != newItem.getViewType()) {
				return false
			}
			
			when (oldItem) {
				is ProfileItem -> {
					val item = newItem as ProfileItem
					return oldItem.nickname == item.nickname && oldItem.location == item.location
							&& oldItem.isLocationTracking && item.isLocationTracking
				}
				is UserStatusItem -> {
					val item = newItem as UserStatusItem
					return oldItem.dutyCount == item.dutyCount && oldItem.reportedCount == item.reportedCount &&
							oldItem.reviewedCount == item.reviewedCount
				}
				is ReportItem -> {
					val item = newItem as ReportItem
					return oldItem.report.id == item.report.id
							&& oldItem.report.syncState == item.report.syncState
							&& oldItem.imageState.unsentCount == item.imageState.unsentCount
							&& oldItem.imageState.count == item.imageState.count
				}
				is TitleItem -> {
					val item = newItem as TitleItem
					return oldItem.title == item.title
				}
				is SyncInfoItem -> {
					val item = newItem as SyncInfoItem
					return oldItem.syncInfo.status.name == item.syncInfo.status.name
							&& oldItem.syncInfo.countCheckIn == item.syncInfo.countCheckIn
							&& oldItem.syncInfo.countReport == item.syncInfo.countCheckIn
				}
				else -> return false
			}
		}
	}
	
	interface StatusItemBase {
		fun getViewType(): Int
		
		fun getId(): Int
		
		companion object {
			const val ITEM_TITLE = 0
			const val ITEM_PROFILE = 1
			const val ITEM_USER_STATUS = 2
			const val ITEM_REPORT_HISTORY = 3
			const val ITEM_REPORT_EMPTY = 4
			const val ITEM_SYNC_INFO = 5
			const val ITEM_ALERT = 6
			const val ITEM_SEE_MORE = 7
			const val ITEM_ALERT_EMPTY = 8
			const val ITEM_ALERT_SET_GUARDIAN_GROUP = 9
			const val ITEM_ALERT_LOADING = 10
			
		}
	}
	
	data class ProfileItem(val nickname: String, val location: String, val isLocationTracking: Boolean) : StatusItemBase {
		override fun getId() = -1
		
		override fun getViewType(): Int = ITEM_PROFILE
		
		fun getProfileName(): String = nickname.trim().capitalize()
	}
	
	data class SyncInfoItem(val syncInfo: SyncInfo) : StatusItemBase {
		override fun getViewType(): Int = ITEM_SYNC_INFO
		
		override fun getId(): Int = -7
		
		fun getIcon(): Int = when (syncInfo.status) {
			SyncInfo.Status.WAITING_NETWORK -> R.drawable.ic_queue
			SyncInfo.Status.STARTING -> R.drawable.ic_upload
			SyncInfo.Status.UPLOADING -> R.drawable.ic_upload
			else -> R.drawable.ic_upload_done // upload completed
		}
		
		fun getSyncingTitle(context: Context): String = when (syncInfo.status) {
			SyncInfo.Status.WAITING_NETWORK, SyncInfo.Status.STARTING, SyncInfo.Status.UPLOADING -> {
				val checkinText = if (syncInfo.countCheckIn > 0) context.getString(R.string.sync_checkins_label, syncInfo.countCheckIn) else null
				val reportText = if (syncInfo.countReport > 0) {
					context.getString(
							if (syncInfo.countReport > 1) R.string.sync_reports_label else R.string.sync_report_label, syncInfo.countReport)
				} else null
				val result = if (checkinText != null && reportText != null) "$reportText, $checkinText" else reportText
						?: checkinText
				
				result ?: " - " // TODO: handle when title null
			}
			else -> context.getString(R.string.sync_complete)  // upload completed
		}
		
		fun getSyncingDescription(context: Context): String = when (syncInfo.status) {
			SyncInfo.Status.WAITING_NETWORK -> context.getString(R.string.sync_waiting_network)
			SyncInfo.Status.STARTING -> context.getString(R.string.sync_starting)
			SyncInfo.Status.UPLOADING -> context.getString(R.string.sync_uploading)
			else -> ""  // upload completed
		}
		
		fun isLoading(): Boolean = syncInfo.status == SyncInfo.Status.UPLOADING
	}
	
	data class UserStatusItem(val dutyCount: Long, val reportedCount: Int, val reviewedCount: Int) : StatusItemBase {
		override fun getId(): Int = -2
		
		override fun getViewType(): Int = ITEM_USER_STATUS
	}
	
	data class SeeMoreItem(val text: String) : StatusItemBase {
		override fun getId(): Int = -8
		
		override fun getViewType(): Int = ITEM_SEE_MORE
	}
	
	data class ReportItem(val report: Report, val imageState: ImageState) : StatusItemBase {
		override fun getId(): Int = report.id
		
		override fun getViewType(): Int = ITEM_REPORT_HISTORY
		
		fun getReportType(context: Context): String = report.value.toEventName(context)
		
		fun getIcon(): Int = report.value.toEventIcon()
		
		fun getTextImageState(context: Context): String {
			val attachImagesCount = imageState.count
			val attachImagesUnSyncCount = imageState.unsentCount
			if (attachImagesCount < 1)
				return ""
			
			return if (attachImagesUnSyncCount < 1) {
				context.getString(if (attachImagesCount < 2) R.string.format_image_synced
				else R.string.format_images_synced, attachImagesCount.toString())
			} else {
				context.getString(if (attachImagesCount < 2) R.string.format_image_unsync
				else R.string.format_images_unsync, attachImagesCount.toString(),
						attachImagesUnSyncCount.toString())
			}
		}
		
		fun getTimeAgo(context: Context): String {
			return report.reportedAt.toTimeSinceString(context)
		}
	}
	
	data class AlertItem(var event: Event, var state: State) : StatusItemBase {
		val count = event.confirmedCount + event.rejectedCount
		
		enum class State {
			CONFIRM, REJECT, NONE
		}
		
		override fun getViewType(): Int = ITEM_ALERT
		override fun getId(): Int = -3
		
		fun getGuardianShortname(): String = event.guardianName
		fun getImage(): Int = event.value.toEventIcon()
		fun getTime(context: Context): String = "  ${event.beginsAt.toTimeSinceStringAlternativeTimeAgo(context)}"
		fun getReviewed(context: Context): String = context.getString(if (event.firstNameReviewer.isNotBlank() || state !== State.NONE) R.string.last_reviewed_by else R.string.not_have_review)
		
		fun getConfirmIcon(): Int = when (state) {
			State.CONFIRM -> R.drawable.ic_confirm_event_white
			State.NONE -> R.drawable.ic_confirm_event_gray
			else -> R.drawable.ic_confirm_event_gray
		}
		
		fun getRejectIcon(): Int = when (state) {
			State.REJECT -> R.drawable.ic_reject_event_white
			State.NONE -> R.drawable.ic_reject_event_gray
			else -> R.drawable.ic_reject_event_gray
		}
		
		fun isVisibility(): Boolean = state == State.NONE
	}
	
	
	class ReportEmpty : StatusItemBase {
		override fun getViewType(): Int = ITEM_REPORT_EMPTY
		
		override fun getId(): Int = -6
	}
	
	class AlertEmpty : StatusItemBase {
		override fun getViewType(): Int = ITEM_ALERT_EMPTY
		
		override fun getId(): Int = -4
	}
	
	class AlertSetGuardianGroupItem : StatusItemBase {
		override fun getId(): Int = -5
		
		override fun getViewType(): Int = ITEM_ALERT_SET_GUARDIAN_GROUP
	}
	
	class AlertLoading : StatusItemBase {
		override fun getViewType(): Int = ITEM_ALERT_LOADING
		
		override fun getId(): Int = -9
	}
}

class AlertLoadingView(itemView: View) : RecyclerView.ViewHolder(itemView)