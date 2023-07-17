package org.rfcx.incidents.view.report.deployment.detail.display

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.databinding.ItemDisplayImageBinding
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setDeploymentImage

class DisplayImageAdapter(private val imageList: List<String>, private val context: Context) :
    RecyclerView.Adapter<DisplayImageAdapter.DisplayImageViewHolder>() {

    private lateinit var binding: ItemDisplayImageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayImageViewHolder {
        binding = ItemDisplayImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DisplayImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DisplayImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    inner class DisplayImageViewHolder(binding: ItemDisplayImageBinding) : RecyclerView.ViewHolder(binding.root) {
        private val imageView = binding.displayImage
        private val progressBar = binding.progressBarOfImageView

        fun bind(item: String) {
            val token = context.getTokenID()
            val fromServer = !item.startsWith("file")
            imageView.setDeploymentImage(
                url = item,
                blur = false,
                fromServer = fromServer,
                token = token,
                progressBar = progressBar
            )
        }
    }
}
