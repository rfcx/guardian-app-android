package org.rfcx.ranger.view.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_feedback_image.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseListItem
import org.rfcx.ranger.util.GlideApp

class FeedbackImageAdapter : ListAdapter<BaseListItem, RecyclerView.ViewHolder>(FeedbackImageAdapterDiffUtil()) {
	
	companion object {
		const val MAX_IMAGE_SIZE = 5
		const val VIEW_TYPE_IMAGE = 1
		const val VIEW_TYPE_ADD_IMAGE = 2
	}
	
	var onFeedbackImageAdapterClickListener: OnFeedbackImageAdapterClickListener? = null
	
	private var imagesSource = arrayListOf<BaseListItem>()
	
	fun addImages(uris: List<String>) {
		var index: Int = if (imagesSource.isEmpty()) 0 else {
			imagesSource[imagesSource.count() - 1].getItemId() + 1
		}
		uris.forEach {
			imagesSource.add(LocalImageItem(index, it, true))
			index++
		}
		submitList(ArrayList(imagesSource))
	}
	
	fun removeAt(index: Int) {
		if (getItem(imagesSource.count() - 1) is AddImageItem) {
			imagesSource.removeAt(imagesSource.count() - 1)
		}
		imagesSource.removeAt(index)
		submitList(ArrayList(imagesSource))
	}
	
	fun getImageCount(): Int {
		return imagesSource.count()
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_feedback_image, parent, false)
		return FeedbackImageAdapterViewHolder(view, onFeedbackImageAdapterClickListener)
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (holder is FeedbackImageAdapterViewHolder && getItem(position) is LocalImageItem) {
			val itemImage = getItem(position) as LocalImageItem
			holder.bind(itemImage.localPath)
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
	
	inner class FeedbackImageAdapterViewHolder(itemView: View, private val onFeedbackImageAdapterClickListener: OnFeedbackImageAdapterClickListener?) : RecyclerView.ViewHolder(itemView) {
		fun bind(imagePath: String) {
			
			val text: List<String>? = imagePath.split("/")
			itemView.nameImageTextView.text = text?.get(text.size - 1) ?: ""
			
			GlideApp.with(itemView.imageFeedbackImageView)
					.load(imagePath)
					.placeholder(R.drawable.ic_mountains)
					.error(R.drawable.ic_mountains)
					.into(itemView.imageFeedbackImageView)
			
			itemView.deleteImageFeedbackButton.setOnClickListener {
				onFeedbackImageAdapterClickListener?.onDeleteImageClick(adapterPosition)
			}
		}
	}
	
	class FeedbackImageAdapterDiffUtil : DiffUtil.ItemCallback<BaseListItem>() {
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

interface OnFeedbackImageAdapterClickListener {
	fun onDeleteImageClick(position: Int)
}
