package org.rfcx.incidents.view.profile.guardian

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.databinding.ItemSoftwareDownloadBinding
import org.rfcx.incidents.entity.guardian.FileStatus
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.Software

class SoftwareDownloadAdapter(private val listener: SoftwareEventListener) : RecyclerView.Adapter<SoftwareDownloadAdapter.SoftwareDownloadViewHolder>() {

    private lateinit var binding: ItemSoftwareDownloadBinding

    var availableSoftwares: List<GuardianFile> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var downloadedSoftwares: List<Software> = listOf()
        set(value) {
            field = value
        }

    var needLoading = false
    var selected = -1

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): SoftwareDownloadViewHolder {
        binding = ItemSoftwareDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoftwareDownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoftwareDownloadViewHolder, position: Int) {
        holder.bind(availableSoftwares[position])
        holder.deleteButton.setOnClickListener {
            selected = position
            listener.onDeleteClicked(availableSoftwares[position].file as SoftwareResponse)
        }
        holder.downloadButton.setOnClickListener {
            selected = position
            needLoading = true
            notifyDataSetChanged()
            listener.onDownloadClicked(availableSoftwares[position].file as SoftwareResponse)
        }
    }

    override fun getItemCount(): Int = availableSoftwares.size

    inner class SoftwareDownloadViewHolder(binding: ItemSoftwareDownloadBinding) : RecyclerView.ViewHolder(binding.root) {
        private val name = binding.softwareName
        private val status = binding.softwareStatus
        val downloadButton: Button = binding.softwareDownloadButton
        val deleteButton: Button = binding.softwareDeleteButton
        private val loading = binding.downloadLoading

        fun bind(file: GuardianFile) {
            val software = (file.file as SoftwareResponse)
            val downloadedSoftware = downloadedSoftwares.findLast { it.role == software.role }
            name.text = software.role
            when (file.status) {
                FileStatus.NOT_DOWNLOADED -> {
                    status.visibility = View.VISIBLE
                    downloadButton.isEnabled = true
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.text = "Download"
                    deleteButton.visibility = View.GONE
                }
                FileStatus.NEED_UPDATE -> {
                    status.visibility = View.GONE
                    downloadButton.isEnabled = true
                    downloadButton.visibility = View.VISIBLE
                    deleteButton.isEnabled = true
                    deleteButton.visibility = View.VISIBLE
                }
                FileStatus.UP_TO_DATE -> {
                    status.visibility = View.GONE
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.isEnabled = false
                    downloadButton.text = "Up to date"
                    deleteButton.isEnabled = true
                    deleteButton.visibility = View.VISIBLE
                    deleteButton.text = "delete v${downloadedSoftware!!.version}"
                }
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

interface SoftwareEventListener {
    fun onDownloadClicked(software: SoftwareResponse)
    fun onDeleteClicked(software: SoftwareResponse)
}
