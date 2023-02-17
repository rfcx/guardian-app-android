package org.rfcx.incidents.view.guardian.checklist.softwareupdate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemGuardianFileChildBinding
import org.rfcx.incidents.databinding.ItemGuardianFileHeaderBinding
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileUpdateItem
import org.rfcx.incidents.entity.guardian.UpdateStatus

class GuardianFileUpdateAdapter(
    private var childrenClickedListener: ChildrenClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var headerBinding: ItemGuardianFileHeaderBinding
    private lateinit var childBinding: ItemGuardianFileChildBinding

    companion object {
        const val VERSION_ITEM = 1
        const val HEADER_ITEM = 2
    }

    var files: List<GuardianFileUpdateItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VERSION_ITEM -> {
                childBinding = ItemGuardianFileChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SoftwareVersionViewHolder(childBinding)
            }

            else -> {
                headerBinding = ItemGuardianFileHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SoftwareHeaderViewHolder(headerBinding)
            }
        }
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VERSION_ITEM -> {
                (holder as SoftwareVersionViewHolder).bind(files[position] as GuardianFileUpdateItem.GuardianFileUpdateVersion, childrenClickedListener)
            }
            else -> {
                (holder as SoftwareHeaderViewHolder).bind(files[position] as GuardianFileUpdateItem.GuardianFileUpdateHeader)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (files[position]) {
            is GuardianFileUpdateItem.GuardianFileUpdateVersion -> VERSION_ITEM
            else -> HEADER_ITEM
        }
    }

    class SoftwareHeaderViewHolder(itemView: ItemGuardianFileHeaderBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var appName: TextView = itemView.fileNameTextView

        fun bind(file: GuardianFileUpdateItem.GuardianFileUpdateHeader) {
            appName.text = file.name
        }
    }

    class SoftwareVersionViewHolder(itemView: ItemGuardianFileChildBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var apkVersion: TextView = itemView.fileVersionTextView
        private var apkInstalled: TextView = itemView.fileInstalledVersionTextView
        private var apkSendButton: Button = itemView.fileSendButton
        private var apkUpToDateText: TextView = itemView.fileUpToDateTextView
        private var apkLoading: LinearProgressIndicator = itemView.fileLoading

        fun bind(file: GuardianFileUpdateItem.GuardianFileUpdateVersion, listener: ChildrenClickedListener) {
            apkVersion.text = "v${file.updateFile?.version}" ?: "-"
            apkVersion.apply {
                apkInstalled.text = context.getString(R.string.installed_guardian_file, file.installedVersion)
                when(file.status) {
                    UpdateStatus.LOADING -> {
                        apkLoading.visibility = View.VISIBLE
                        apkSendButton.visibility = View.GONE
                        // if (file.progress != null && file.progress != 100) {
                        //     apkLoading.setProgressCompat(file.progress, true)
                        // } else {
                            apkLoading.isIndeterminate = true
                        // }
                    }
                    UpdateStatus.UP_TO_DATE -> {
                        apkSendButton.visibility = View.GONE
                        apkUpToDateText.visibility = View.VISIBLE
                        apkLoading.visibility = View.GONE
                    }
                    UpdateStatus.NEED_UPDATE -> {
                        apkSendButton.isEnabled = true
                        apkSendButton.visibility = View.VISIBLE
                        apkSendButton.text = "update to ${file.updateFile?.version}"
                        apkUpToDateText.visibility = View.GONE
                        apkLoading.visibility = View.GONE
                    }
                    UpdateStatus.NOT_INSTALLED -> {
                        apkSendButton.isEnabled = true
                        apkSendButton.visibility = View.VISIBLE
                        apkSendButton.text = "update to ${file.updateFile?.version}"
                        apkUpToDateText.visibility = View.GONE
                        apkLoading.visibility = View.GONE
                    }
                    UpdateStatus.WAITING -> {
                        apkSendButton.isEnabled = false
                        apkSendButton.visibility = View.VISIBLE
                        apkSendButton.text = "waiting"
                        apkUpToDateText.visibility = View.GONE
                        apkLoading.visibility = View.GONE
                    }
                    UpdateStatus.ACTIVATED -> {

                    }
                    UpdateStatus.DEACTIVATED -> {

                    }
                }
            }

            apkSendButton.setOnClickListener {
                listener.onItemClick(file.updateFile!!)
            }
        }
    }
}

interface ChildrenClickedListener {
    fun onItemClick(selectedFile: GuardianFile)
}
