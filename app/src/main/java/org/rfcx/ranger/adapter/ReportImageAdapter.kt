package org.rfcx.ranger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_report_image.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseListItem
import org.rfcx.ranger.util.GlideApp

class ReportImageAdapter : ListAdapter<BaseListItem, RecyclerView.ViewHolder>(ReportImageAdapterDiffUtil()) {
	
	companion object {
		const val VIEW_TYPE_IMAGE = 1
		const val VIEW_TYPE_ADD_IMAGE = 2
		const val MAX_IMAGE_SIZE = 5
	}
	
	var onReportImageAdapterClickListener: OnReportImageAdapterClickListener? = null
	
	fun setImages(oldReportImageAttachmentsCount: Int, images: List<String>) {
		val newItems = arrayListOf<BaseListItem>()
		var position = 0
		images.forEach {
			newItems.add(ImageItem(position, it, position >= oldReportImageAttachmentsCount))
			position++
		}
		if (newItems.count() < MAX_IMAGE_SIZE) {
			newItems.add(AddImageItem())
		}
		
		submitList(newItems)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		
		return when (viewType) {
			VIEW_TYPE_IMAGE -> {
				val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_report_image, parent, false)
				ReportImageAdapterViewHolder(view, onReportImageAdapterClickListener)
			}
			VIEW_TYPE_ADD_IMAGE -> {
				val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_image_report, parent, false)
				AddImageViewHolder(view, onReportImageAdapterClickListener)
			}
			else -> throw IllegalAccessException("View type $viewType not found.")
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is ReportImageAdapterViewHolder && getItem(position) is ImageItem) {
			val itemImage = getItem(position) as ImageItem
			holder.bind(itemImage.imagePath, itemImage.canDelete)
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is ImageItem -> VIEW_TYPE_IMAGE
			is AddImageItem -> VIEW_TYPE_ADD_IMAGE
			else -> throw IllegalStateException("Item class not found ${getItem(position)::class.java.simpleName}")
		}
	}
	
	inner class ReportImageAdapterViewHolder(itemView: View, private val onReportImageAdapterClickListener: OnReportImageAdapterClickListener?) : RecyclerView.ViewHolder(itemView) {
		
		fun bind(imagePath: String, canDelete: Boolean) {
			GlideApp.with(itemView.imageReport)
					.load(imagePath)
					.into(itemView.imageReport)
			
			itemView.deleteImageButton.visibility = if (canDelete) View.VISIBLE else View.INVISIBLE
			itemView.deleteImageButton.setOnClickListener {
				onReportImageAdapterClickListener?.onDeleteImageClick(adapterPosition)
			}
		}
	}
	
	inner class AddImageViewHolder(itemView: View, onReportImageAdapterClickListener: OnReportImageAdapterClickListener?) : RecyclerView.ViewHolder(itemView) {
		init {
			itemView.setOnClickListener {
				onReportImageAdapterClickListener?.onAddImageClick()
			}
		}
	}
	
	
	class ReportImageAdapterDiffUtil : DiffUtil.ItemCallback<BaseListItem>() {
		override fun areItemsTheSame(oldItem: BaseListItem, newItem: BaseListItem): Boolean {
			return oldItem.getItemId() == newItem.getItemId()
		}
		
		override fun areContentsTheSame(oldItem: BaseListItem, newItem: BaseListItem): Boolean {
			return if (newItem is ImageItem && oldItem is ImageItem) {
				(newItem.imageId == oldItem.imageId && newItem.imagePath == oldItem.imagePath)
			} else newItem is AddImageItem && oldItem is AddImageItem
		}
	}
	
}

data class ImageItem(val imageId: Int, val imagePath: String, val canDelete: Boolean) : BaseListItem {
	override fun getItemId(): Int = imageId
}

data class AddImageItem(val any: Any? = null) : BaseListItem {
	override fun getItemId(): Int = -11
}

interface OnReportImageAdapterClickListener {
	fun onAddImageClick()
	fun onDeleteImageClick(position: Int)
}

