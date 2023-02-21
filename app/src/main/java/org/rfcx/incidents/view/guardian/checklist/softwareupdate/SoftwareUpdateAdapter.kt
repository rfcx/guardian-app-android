package org.rfcx.incidents.view.guardian.checklist.softwareupdate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemGuardianFileChildSoftwareBinding
import org.rfcx.incidents.databinding.ItemGuardianFileHeaderBinding
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.SoftwareUpdateItem
import org.rfcx.incidents.entity.guardian.UpdateStatus

class SoftwareUpdateAdapter(
    private var childrenClickedListener: ChildrenClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var headerBinding: ItemGuardianFileHeaderBinding
    private lateinit var childBinding: ItemGuardianFileChildSoftwareBinding

    companion object {
        const val VERSION_ITEM = 1
        const val HEADER_ITEM = 2
    }

    var files: List<SoftwareUpdateItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VERSION_ITEM -> {
                childBinding = ItemGuardianFileChildSoftwareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                (holder as SoftwareVersionViewHolder).bind(files[position] as SoftwareUpdateItem.SoftwareUpdateVersion, childrenClickedListener)
            }
            else -> {
                (holder as SoftwareHeaderViewHolder).bind(files[position] as SoftwareUpdateItem.SoftwareUpdateHeader)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (files[position]) {
            is SoftwareUpdateItem.SoftwareUpdateVersion -> VERSION_ITEM
            else -> HEADER_ITEM
        }
    }

    class SoftwareHeaderViewHolder(itemView: ItemGuardianFileHeaderBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var appName: TextView = itemView.fileNameTextView

        fun bind(file: SoftwareUpdateItem.SoftwareUpdateHeader) {
            appName.text = file.name
        }
    }

    class SoftwareVersionViewHolder(itemView: ItemGuardianFileChildSoftwareBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var apkVersion: TextView = itemView.fileVersionTextView
        private var apkSendButton: Button = itemView.fileSendButton
        private var apkLoading: LinearProgressIndicator = itemView.fileLoading

        fun bind(file: SoftwareUpdateItem.SoftwareUpdateVersion, listener: ChildrenClickedListener) {
            apkVersion.text = itemView.context.getString(R.string.file_version, file.updateFile?.version, if (file.installedVersion == null) "not" else "v${file.installedVersion}")
            apkSendButton.isEnabled = file.isEnabled
            when(file.status) {
                UpdateStatus.LOADING -> {
                    apkLoading.visibility = View.VISIBLE
                    apkSendButton.visibility = View.GONE
                    if (file.progress != null && file.progress != 100) {
                        apkLoading.setProgressCompat(file.progress, true)
                    } else {
                        apkLoading.isIndeterminate = true
                    }
                }
                UpdateStatus.UP_TO_DATE -> {
                    apkSendButton.isEnabled = false
                    apkSendButton.visibility = View.VISIBLE
                    apkSendButton.text = itemView.context.getString(R.string.up_to_date)
                    apkLoading.visibility = View.GONE
                }
                UpdateStatus.NEED_UPDATE -> {
                    apkSendButton.visibility = View.VISIBLE
                    apkSendButton.text = itemView.context.getString(R.string.file_update, file.updateFile?.version)
                    apkLoading.visibility = View.GONE
                }
                UpdateStatus.NOT_INSTALLED -> {
                    apkSendButton.visibility = View.VISIBLE
                    apkSendButton.text = itemView.context.getString(R.string.file_install, file.updateFile?.version)
                    apkLoading.visibility = View.GONE
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
