package org.rfcx.incidents.view.guardian.checklist.classifierupload

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemGuardianFileChildClassifierBinding
import org.rfcx.incidents.databinding.ItemGuardianFileHeaderBinding
import org.rfcx.incidents.entity.guardian.ClassifierUploadItem
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.UpdateStatus

class ClassifierUploadAdapter(
    private var childrenClickedListener: ChildrenClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var headerBinding: ItemGuardianFileHeaderBinding
    private lateinit var childBinding: ItemGuardianFileChildClassifierBinding

    companion object {
        const val VERSION_ITEM = 1
        const val HEADER_ITEM = 2
    }

    var files: List<ClassifierUploadItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VERSION_ITEM -> {
                childBinding = ItemGuardianFileChildClassifierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ClassifierVersionViewHolder(childBinding)
            }

            else -> {
                headerBinding = ItemGuardianFileHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ClassifierHeaderViewHolder(headerBinding)
            }
        }
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VERSION_ITEM -> {
                (holder as ClassifierVersionViewHolder).bind(files[position] as ClassifierUploadItem.ClassifierUploadVersion, childrenClickedListener)
            }
            else -> {
                (holder as ClassifierHeaderViewHolder).bind(files[position] as ClassifierUploadItem.ClassifierUploadHeader)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (files[position]) {
            is ClassifierUploadItem.ClassifierUploadVersion -> VERSION_ITEM
            else -> HEADER_ITEM
        }
    }

    class ClassifierHeaderViewHolder(itemView: ItemGuardianFileHeaderBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var appName: TextView = itemView.fileNameTextView

        fun bind(file: ClassifierUploadItem.ClassifierUploadHeader) {
            appName.text = file.name
        }
    }

    class ClassifierVersionViewHolder(itemView: ItemGuardianFileChildClassifierBinding) : RecyclerView.ViewHolder(itemView.root) {
        private var classifierVersion: TextView = itemView.fileVersionTextView
        private var classifierSendButton: Button = itemView.fileSendButton
        private var classifierActiveButton: Button = itemView.fileActivateButton
        private var classifierDeActiveButton: Button = itemView.fileDeActivateButton
        private var classifierLoading: LinearProgressIndicator = itemView.fileLoading

        fun bind(file: ClassifierUploadItem.ClassifierUploadVersion, listener: ChildrenClickedListener) {
            classifierVersion.text = itemView.context.getString(
                R.string.file_version,
                file.updateFile?.version,
                if (file.installedVersion == null) "not" else "v${file.installedVersion}"
            )
            classifierSendButton.isEnabled = file.isEnabled
            classifierActiveButton.isEnabled = file.isEnabled
            classifierDeActiveButton.isEnabled = file.isEnabled
            classifierActiveButton.visibility = if (file.isActive) View.GONE else View.VISIBLE
            classifierDeActiveButton.visibility = if (file.isActive) View.VISIBLE else View.GONE
            when (file.status) {
                UpdateStatus.LOADING -> {
                    classifierLoading.visibility = View.VISIBLE
                    classifierSendButton.visibility = View.GONE
                    classifierActiveButton.visibility = View.GONE
                    classifierDeActiveButton.visibility = View.GONE
                    if (file.progress != null && file.progress != 100) {
                        classifierLoading.setProgressCompat(file.progress, true)
                    } else {
                        classifierLoading.isIndeterminate = true
                    }
                }
                UpdateStatus.UP_TO_DATE -> {
                    classifierSendButton.isEnabled = false
                    classifierSendButton.visibility = View.VISIBLE
                    classifierSendButton.text = itemView.context.getString(R.string.up_to_date)
                    classifierLoading.visibility = View.GONE
                }
                UpdateStatus.NEED_UPDATE -> {
                    classifierSendButton.visibility = View.VISIBLE
                    classifierSendButton.text = itemView.context.getString(R.string.file_update, file.updateFile?.version)
                    classifierLoading.visibility = View.GONE
                }
                UpdateStatus.NOT_INSTALLED -> {
                    classifierSendButton.visibility = View.VISIBLE
                    classifierSendButton.text = itemView.context.getString(R.string.file_install, file.updateFile?.version)
                    classifierLoading.visibility = View.GONE
                    classifierActiveButton.visibility = View.GONE
                    classifierDeActiveButton.visibility = View.GONE
                }
            }

            classifierSendButton.setOnClickListener {
                listener.onUploadClick(file.updateFile!!)
            }
            classifierActiveButton.setOnClickListener {
                listener.onActivateClick(file.updateFile!!)
            }
            classifierDeActiveButton.setOnClickListener {
                listener.onDeActivateClick(file.updateFile!!)
            }
        }
    }
}

interface ChildrenClickedListener {
    fun onUploadClick(selectedFile: GuardianFile)
    fun onActivateClick(selectedFile: GuardianFile)
    fun onDeActivateClick(selectedFile: GuardianFile)
}
