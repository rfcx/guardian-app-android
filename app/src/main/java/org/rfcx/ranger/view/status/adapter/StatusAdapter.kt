package org.rfcx.ranger.view.status.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.TitleItem
import org.rfcx.ranger.adapter.view.TitleViewHolder
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.databinding.ItemStatusReportBinding
import org.rfcx.ranger.databinding.ItemUserStatusBinding
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.toEventIcon
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_PROFILE
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_REPORT_HISTORY
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_TITLE
import org.rfcx.ranger.view.status.adapter.StatusAdapter.StatusItemBase.Companion.ITEM_USER_STATUS
import org.rfcx.ranger.view.status.adapter.viewholder.ProfileView
import org.rfcx.ranger.view.status.adapter.viewholder.ReportView
import org.rfcx.ranger.view.status.adapter.viewholder.UserStatusView

class StatusAdapter : ListAdapter<StatusAdapter.StatusItemBase, RecyclerView.ViewHolder>(StatusListDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_PROFILE -> {
                val itemView = DataBindingUtil.inflate<ItemHeaderProfileBinding>(inflater, R.layout.item_header_profile, parent, false)
                ProfileView(itemView)
            }
            ITEM_USER_STATUS -> {
                val itemView = DataBindingUtil.inflate<ItemUserStatusBinding>(inflater, R.layout.item_user_status, parent, false)
                UserStatusView(itemView)
            }
            ITEM_REPORT_HISTORY -> {
                val itemView = DataBindingUtil.inflate<ItemStatusReportBinding>(inflater,R.layout.item_status_report, parent, false)
                ReportView(itemView)
            }
            ITEM_TITLE -> {
                val itemView = inflater.inflate(R.layout.item_title_holder, parent, false)
                TitleViewHolder(itemView)
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
            is ReportView -> {
                holder.bind(item as ReportItem)
            }
            is TitleViewHolder -> {
                holder.bind((item as TitleItem).title)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).getViewType()
    }

    class StatusListDiffUtil : DiffUtil.ItemCallback<StatusItemBase>() {
        override fun areItemsTheSame(oldItem: StatusItemBase, newItem: StatusItemBase): Boolean {
            return oldItem.getViewType() == newItem.getViewType()
        }

        override fun areContentsTheSame(oldItem: StatusItemBase, newItem: StatusItemBase): Boolean {
            return false
        }
    }

    interface StatusItemBase {
        fun getViewType(): Int

        companion object {
            const val ITEM_TITLE = 0
            const val ITEM_PROFILE = 1
            const val ITEM_USER_STATUS = 2
            const val ITEM_REPORT_HISTORY = 3
        }
    }

    data class ProfileItem(val nickname: String, val location: String, val isLocationTracking: Boolean) : StatusItemBase {
        override fun getViewType(): Int = ITEM_PROFILE

        fun getProfileName(): String = nickname.trim().capitalize()
    }

    data class UserStatusItem(val dutyCount: Int, val reportedCount: Int, val reviewedCount: Int) : StatusItemBase {
        override fun getViewType(): Int = ITEM_USER_STATUS
    }

    data class ReportItem(val report: Report, val attachImagesCount: Int, val attachImagesUnSyncCount: Int) : StatusItemBase {
        override fun getViewType(): Int = ITEM_REPORT_HISTORY

        fun getIcon():Int = report.value.toEventIcon()

        fun getLatLng(): String = "${report.latitude}, ${report.longitude}"
    }
}