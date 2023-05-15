package org.rfcx.incidents.view.profile.guardian

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemGuardianFileDownloadBinding
import org.rfcx.incidents.entity.guardian.file.FileStatus
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileItem

class GuardianFileDownloadAdapter(private val listener: GuardianFileEventListener) :
    RecyclerView.Adapter<GuardianFileDownloadAdapter.FileDownloadViewHolder>() {

    private lateinit var binding: ItemGuardianFileDownloadBinding

    var availableFiles: List<GuardianFileItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var needLoading = false
    var selected = -1

    fun showLoading() {
        needLoading = true
        notifyDataSetChanged()
    }

    fun hideLoading() {
        needLoading = false
        selected = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): FileDownloadViewHolder {
        binding = ItemGuardianFileDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileDownloadViewHolder, position: Int) {
        holder.bind(availableFiles[position])
        holder.deleteButton.setOnClickListener {
            selected = position
            listener.onDeleteClicked(availableFiles[position].local!!)
        }
        holder.downloadButton.setOnClickListener {
            selected = position
            listener.onDownloadClicked(availableFiles[position].remote!!)
        }
    }

    override fun getItemCount(): Int = availableFiles.size

    inner class FileDownloadViewHolder(binding: ItemGuardianFileDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        private val name = binding.fileName
        private val status = binding.fileStatus
        val downloadButton: Button = binding.fileDownloadButton
        val deleteButton: Button = binding.fileDeleteButton
        private val loading = binding.downloadLoading

        fun bind(file: GuardianFileItem) {
            name.text = file.local?.name ?: file.remote!!.name
            when (file.status) {
                FileStatus.NOT_DOWNLOADED -> {
                    status.visibility = View.VISIBLE
                    downloadButton.isEnabled = true
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.text = itemView.context.getString(R.string.download)
                }
                FileStatus.NEED_DOWNLOAD -> {
                    status.visibility = View.GONE
                    downloadButton.isEnabled = true
                    downloadButton.visibility = View.VISIBLE
                }
                FileStatus.UP_TO_DATE -> {
                    status.visibility = View.GONE
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.isEnabled = false
                    downloadButton.text = itemView.context.getString(R.string.up_to_date)
                }
                else -> {
                    status.visibility = View.GONE
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.isEnabled = false
                    downloadButton.text = itemView.context.getString(R.string.unavailable)
                }
            }

            if (file.local != null) {
                deleteButton.isEnabled = true
                deleteButton.visibility = View.VISIBLE
                deleteButton.text = itemView.context.getString(R.string.file_delete, file.local.version)
            } else {
                deleteButton.visibility = View.GONE
            }

            if (needLoading && adapterPosition != selected) {
                downloadButton.isEnabled = false
                deleteButton.isEnabled = false
                loading.visibility = View.GONE
            } else if (needLoading && adapterPosition == selected) {
                downloadButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
                loading.visibility = View.VISIBLE
            } else {
                loading.visibility = View.GONE
            }
        }
    }
}

interface GuardianFileEventListener {
    fun onDownloadClicked(file: GuardianFile)
    fun onDeleteClicked(file: GuardianFile)
}
