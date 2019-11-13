package org.rfcx.ranger.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_report_image.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseListItem
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.GlideApp

class ReportImageAdapter : ListAdapter<BaseListItem, RecyclerView.ViewHolder>(ReportImageAdapterDiffUtil()) {
	
	companion object {
		const val VIEW_TYPE_IMAGE = 1
		const val VIEW_TYPE_ADD_IMAGE = 2
		const val MAX_IMAGE_SIZE = 5
	}
	
	var onReportImageAdapterClickListener: OnReportImageAdapterClickListener? = null
	private var context: Context? = null
	private var imagesSource = arrayListOf<BaseListItem>()
	
	fun setImages(reportImages: List<ReportImage>) {
		imagesSource = arrayListOf()
		var index = 0
		reportImages.forEach {
			Log.d("setImages", "${it.remotePath}")
			if (it.remotePath != null) {
				imagesSource.add(RemoteImageItem(index, it.remotePath!!, false))
			} else {
				imagesSource.add(LocalImageItem(index, it.localPath, it.syncState == ReportImageDb.UNSENT))
			}
			index++
		}
		if (imagesSource.count() < MAX_IMAGE_SIZE) {
			imagesSource.add(AddImageItem())
		}
		
		submitList(ArrayList(imagesSource))
	}
	
	fun addImages(uris: List<String>) {
		val allLocalPathImages = getNewAttachImage() + uris
		val groups = allLocalPathImages.groupBy { it }
		val localPathImages = groups.filter { it.value.size < 2 }
		val localPathImagesForAdd = ArrayList<String>()
		
		localPathImages.forEach {
			if (it.key !in getNewAttachImage()) {
				localPathImagesForAdd.add(it.key)
			}
		}
		
		if (getItem(imagesSource.count() - 1) is AddImageItem) {
			imagesSource.removeAt(imagesSource.count() - 1)
		}
		var index: Int = if (imagesSource.isEmpty()) 0 else {
			imagesSource[imagesSource.count() - 1].getItemId() + 1
		}
		
		if (localPathImagesForAdd.isNotEmpty()) {
			if (localPathImagesForAdd.size != uris.size){
				Toast.makeText(context, R.string.some_photo_already_exists, Toast.LENGTH_SHORT).show()
			}
			
			localPathImagesForAdd.forEach {
				imagesSource.add(LocalImageItem(index, it, true))
				index++
			}
		} else {
			if (uris.size > 1) {
				Toast.makeText(context, R.string.these_photos_already_exists, Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, R.string.this_photo_already_exists, Toast.LENGTH_SHORT).show()
			}
		}
		
		if (imagesSource.count() < MAX_IMAGE_SIZE) {
			imagesSource.add(AddImageItem())
		}
		submitList(ArrayList(imagesSource))
	}
	
	fun removeAt(index: Int) {
		if (getItem(imagesSource.count() - 1) is AddImageItem) {
			imagesSource.removeAt(imagesSource.count() - 1)
		}
		
		imagesSource.removeAt(index)
		
		if (imagesSource.count() < MAX_IMAGE_SIZE) {
			imagesSource.add(AddImageItem())
		}
		submitList(ArrayList(imagesSource))
	}
	
	fun getNewAttachImage(): List<String> {
		return imagesSource.filter {
			(it is LocalImageItem && it.canDelete)
		}.map {
			(it as LocalImageItem).localPath
		}
	}
	
	fun getImageCount(): Int = if (imagesSource[imagesSource.count() - 1] is AddImageItem) imagesSource.count() - 1
	else imagesSource.count()
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		context = parent.context
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
		if (holder is ReportImageAdapterViewHolder && getItem(position) is LocalImageItem) {
			val itemImage = getItem(position) as LocalImageItem
			holder.bind(itemImage.localPath, itemImage.canDelete)
		} else if (holder is ReportImageAdapterViewHolder && getItem(position) is RemoteImageItem) {
			val itemImage = getItem(position) as RemoteImageItem
			holder.bind(itemImage.remotePath, false)
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is LocalImageItem -> VIEW_TYPE_IMAGE
			is RemoteImageItem -> VIEW_TYPE_IMAGE
			is AddImageItem -> VIEW_TYPE_ADD_IMAGE
			else -> throw IllegalStateException("Item class not found ${getItem(position)::class.java.simpleName}")
		}
	}
	
	inner class ReportImageAdapterViewHolder(itemView: View, private val onReportImageAdapterClickListener: OnReportImageAdapterClickListener?) : RecyclerView.ViewHolder(itemView) {
		
		fun bind(imagePath: String, canDelete: Boolean) {
			GlideApp.with(itemView.imageReport)
					.load(imagePath)
					.placeholder(R.drawable.bg_grey_light)
					.error(R.drawable.bg_grey_light)
					.into(itemView.imageReport)
			
			itemView.deleteImageButton.visibility = if (canDelete) View.VISIBLE else View.INVISIBLE
			itemView.deleteImageButton.setOnClickListener {
				onReportImageAdapterClickListener?.onDeleteImageClick(adapterPosition, imagePath)
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
			return if (newItem is LocalImageItem && oldItem is LocalImageItem) {
				(newItem.imageId == oldItem.imageId && newItem.localPath == oldItem.localPath)
			} else if (newItem is RemoteImageItem && oldItem is RemoteImageItem) {
				(newItem.imageId == oldItem.imageId && newItem.remotePath == oldItem.remotePath)
			} else newItem is AddImageItem && oldItem is AddImageItem
		}
	}
	
}

data class LocalImageItem(var imageId: Int, val localPath: String, val canDelete: Boolean) : BaseListItem {
	override fun getItemId(): Int = imageId
}

data class RemoteImageItem(var imageId: Int, val remotePath: String, val canDelete: Boolean) : BaseListItem {
	override fun getItemId(): Int = imageId
}

data class AddImageItem(val any: Any? = null) : BaseListItem {
	override fun getItemId(): Int = -11
}

interface OnReportImageAdapterClickListener {
	fun onAddImageClick()
	fun onDeleteImageClick(position: Int, imagePath: String)
}

