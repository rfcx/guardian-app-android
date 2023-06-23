package org.rfcx.incidents.view.report.deployment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemDeploymentBinding
import org.rfcx.incidents.databinding.ItemRegistrationBinding
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.toStringWithTimeZone
import java.util.TimeZone

class DeploymentListAdapter(private val deploymentItemListener: DeploymentItemListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val DEPLOYMENT_ITEM = 1
        const val REGISTRATION_ITEM = 2
    }

    var items: List<ListItem> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.DeploymentListItem -> DEPLOYMENT_ITEM
            else -> REGISTRATION_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DEPLOYMENT_ITEM -> {
                val binding = ItemDeploymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return DeploymentListViewHolder(binding)
            }
            else -> {
                val binding = ItemRegistrationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RegistrationListViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            DEPLOYMENT_ITEM -> (holder as DeploymentListViewHolder).bind(items[position] as ListItem.DeploymentListItem)
            else -> (holder as RegistrationListViewHolder).bind(items[position] as ListItem.RegistrationItem)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class DeploymentListViewHolder(binding: ItemDeploymentBinding) : RecyclerView.ViewHolder(binding.root) {

        private val siteName = binding.siteNameTextView
        private val guardianName = binding.guardianNameTextView
        private val dateTextView = binding.dateTextView
        private val syncIcon = binding.syncIcon
        private val imageIcon = binding.imageIcon
        private val loading = binding.syncIconLoading
        private val imageLoading = binding.syncImageIconLoading
        private val guardianTypeLayout = binding.guardianTypeLayout
        private val guardianTypeImage = binding.guardianTypeImageView
        private val guardianTypeText = binding.guardianTypeTextView

        fun bind(item: ListItem.DeploymentListItem) {
            siteName.text = item.stream.name
            guardianName.text = item.guardianId
            dateTextView.text = item.stream.deployment?.deployedAt?.toStringWithTimeZone(itemView.context, TimeZone.getDefault())

            syncIcon.setOnClickListener {
                if (item.stream.deployment?.syncState == SyncState.UNSENT.value) {
                    deploymentItemListener.onCloudClicked(item.stream.id)
                }
            }

            imageIcon.setOnClickListener {
                if (item.stream.deployment?.getAllImagesState() == SyncState.UNSENT.value) {
                    item.stream.deployment?.let {
                        deploymentItemListener.onImageIconClicked(it.deploymentKey)
                    }
                }
            }

            itemView.setOnClickListener {
                deploymentItemListener.onItemClicked(item.stream.id)
            }

            guardianTypeLayout.visibility = if (item.guardianType == null) View.GONE else View.VISIBLE
            if (item.guardianType != null) {
                guardianTypeText.text = item.guardianType
                when (item.guardianType) {
                    "Cell" -> guardianTypeImage.setImageResource(R.drawable.ic_signal_cellular_alt)
                    "Sat" -> guardianTypeImage.setImageResource(R.drawable.ic_satellite_alt)
                }
            }

            when (item.stream.deployment?.syncState) {
                SyncState.UNSENT.value -> {
                    syncIcon.visibility = View.VISIBLE
                    syncIcon.setBackgroundResource(R.drawable.ic_cloud_upload)
                    loading.visibility = View.GONE
                }
                SyncState.SENDING.value -> {
                    syncIcon.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                }
                SyncState.SENT.value -> {
                    syncIcon.visibility = View.VISIBLE
                    syncIcon.setBackgroundResource(R.drawable.ic_cloud_done)
                    loading.visibility = View.GONE
                }
            }

            when (item.stream.deployment?.getAllImagesState()) {
                SyncState.UNSENT.value -> {
                    imageIcon.visibility = View.VISIBLE
                    imageIcon.setBackgroundResource(R.drawable.ic_image_ready)
                    imageLoading.visibility = View.GONE
                }
                SyncState.SENDING.value -> {
                    imageIcon.visibility = View.GONE
                    imageLoading.visibility = View.VISIBLE
                }
                SyncState.SENT.value -> {
                    imageIcon.visibility = View.VISIBLE
                    imageIcon.setBackgroundResource(R.drawable.ic_image_synced)
                    imageLoading.visibility = View.GONE
                }
            }
        }
    }

    inner class RegistrationListViewHolder(binding: ItemRegistrationBinding) : RecyclerView.ViewHolder(binding.root) {
        private val guardianName = binding.guardianNameTextView
        private val syncIcon = binding.syncIcon
        private val loading = binding.syncIconLoading

        fun bind(item: ListItem.RegistrationItem) {
            guardianName.text = item.guardianId

            syncIcon.setOnClickListener {
                deploymentItemListener.onRegisterClicked(item.registration)
            }

            when (item.registration.syncState) {
                SyncState.UNSENT.value -> {
                    syncIcon.visibility = View.VISIBLE
                    syncIcon.setBackgroundResource(R.drawable.ic_cloud_upload)
                    loading.visibility = View.GONE
                }
                SyncState.SENDING.value -> {
                    syncIcon.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                }
                SyncState.SENT.value -> {
                    syncIcon.visibility = View.VISIBLE
                    syncIcon.setBackgroundResource(R.drawable.ic_cloud_done)
                    loading.visibility = View.GONE
                }
            }
        }
    }
}

interface DeploymentItemListener {
    fun onCloudClicked(id: Int)
    fun onImageIconClicked(deploymentId: String)
    fun onRegisterClicked(registration: GuardianRegistration)
    fun onItemClicked(streamId: Int)
}
