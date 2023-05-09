package org.rfcx.incidents.view.guardian.checklist.photos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.databinding.ItemPhotoAdviseBinding
import org.rfcx.incidents.databinding.ItemSiteBinding
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setDeploymentImage
import java.io.Serializable

class ImageAdapter(
    private val imageClickListener: ImageClickListener,
    private val thumbnails: List<String>
) :
    RecyclerView.Adapter<ImageAdapter.ImageAdapterViewHolder>() {
    private var imageItems = arrayListOf<Image>()
    private var currentPosition = -1
    private var currentType: ImageType = ImageType.NORMAL
    private var maxImages = 10

    private lateinit var binding: ItemPhotoAdviseBinding

    fun setMaxImages(number: Int) {
        maxImages = number
    }

    fun setPlaceHolders(type: List<String>) {
        type.forEachIndexed { index, it ->
            imageItems.add(Image(index + 1, it, ImageType.NORMAL, null, null))
        }
        // For other images that out of type scoped
        if (imageItems.size < maxImages) {
            imageItems.add(
                Image(
                    imageItems.size + 1,
                    ImageType.OTHER.value,
                    ImageType.OTHER,
                    null,
                    null
                )
            )
        }
        notifyDataSetChanged()
    }

    fun updateImagesFromSavedImages(images: List<Image>) {
        var tempCount = itemCount
        images.map { it.copy() }.forEach { image ->
            if (image.type == ImageType.OTHER) {
                if (tempCount == maxImages) {
                    imageItems.removeLast()
                }
                imageItems.add(
                    tempCount - 1,
                    Image(
                        tempCount,
                        ImageType.OTHER.value,
                        ImageType.OTHER,
                        image.path,
                        null,
                        image.isNew
                    )
                )
                tempCount++
            } else {
                imageItems.find { image.name == it.name }?.apply {
                    path = image.path
                    remotePath = image.remotePath
                    isNew = image.isNew
                }
            }
        }
        notifyDataSetChanged()
    }

    fun updateTakeOrChooseImage(path: String) {
        if (currentPosition == -1) return
        if (currentType == ImageType.OTHER) {
            if (itemCount == maxImages) {
                imageItems.removeLast()
            }
            imageItems.add(
                itemCount - 1,
                Image(itemCount, ImageType.OTHER.value, ImageType.OTHER, path, null)
            )
        } else {
            imageItems[currentPosition].path = path
        }
        notifyDataSetChanged()
    }

    fun removeImage(image: Image) {
        if (currentPosition == -1) return
        if (image.type == ImageType.OTHER) {
            if (getAvailableImagesLeft() == 0) {
                imageItems.add(
                    Image(
                        currentPosition,
                        ImageType.OTHER.value,
                        ImageType.OTHER,
                        null,
                        null
                    )
                )
            }
            imageItems.remove(image)
        } else {
            imageItems[currentPosition].path = null
        }
        notifyDataSetChanged()
    }

    private fun getOtherCount() =
        imageItems.filter { it.type == ImageType.OTHER && it.path != null }.size

    private fun getNormalCount() = imageItems.filter { it.type == ImageType.NORMAL }.size
    private fun getAvailableImagesLeft(): Int {
        return (maxImages - (getOtherCount() + getNormalCount()))
    }

    fun getCurrentImagePaths() = imageItems

    fun getCurrentNewImages() = imageItems.filter { it.isNew && it.path != null }

    fun getMissingImages(): List<Image> {
        val missing = imageItems.filter { it.path == null && it.type != ImageType.OTHER }
        if (getExistingImages().find { it.id == 9 } != null) return missing.filter { it.id != 10 }
        if (getExistingImages().find { it.id == 10 } != null) return missing.filter { it.id != 9 }
        return missing
    }

    fun getExistingImages() = imageItems.filter { it.path != null }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageAdapterViewHolder {
        binding = ItemPhotoAdviseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageAdapterViewHolder, position: Int) {
        holder.bind(imageItems[position])

        holder.placeHolderButton.setOnClickListener {
            currentPosition = holder.adapterPosition
            currentType = imageItems[currentPosition].type
            imageClickListener.onPlaceHolderClick(currentPosition)
        }

        holder.imageView.setOnClickListener {
            currentPosition = holder.adapterPosition
            imageClickListener.onImageClick(imageItems[currentPosition])
        }
        holder.deleteButton.setOnClickListener {
            currentPosition = holder.adapterPosition
            imageClickListener.onDeleteClick(imageItems[currentPosition])
        }
    }

    inner class ImageAdapterViewHolder(binding: ItemPhotoAdviseBinding) : RecyclerView.ViewHolder(binding.root) {
        val placeHolderButton: AppCompatButton = binding.placeHolderButton
        val imageView: ImageView = binding.image
        val deleteButton: ImageButton = binding.deleteImageButton

        fun bind(image: Image) {
            if (image.path == null) {
                placeHolderButton.text = image.name
                imageView.visibility = View.GONE
                placeHolderButton.visibility = View.VISIBLE
                deleteButton.visibility = View.GONE
                placeHolderButton.apply {
                    val example =
                        thumbnails.getOrNull(adapterPosition) ?: thumbnails[thumbnails.size - 1]
                    val id = this.context.resources.getIdentifier(
                        "${example}_tbn",
                        "drawable",
                        this.context.packageName
                    )
                    if (id != 0) {
                        ContextCompat.getDrawable(this.context, id)?.let {
                            val drawable = it.mutate()
                            drawable.alpha = 100
                            placeHolderButton.background = drawable
                        }
                    }
                }
            } else {
                val token = itemView.context.getTokenID()
                val path =
                    if (image.remotePath != null) BuildConfig.DEVICE_API_BASE_URL + image.remotePath else "file://${image.path}"
                val fromServer = !path.startsWith("file")
                imageView.setDeploymentImage(path, false, fromServer, token)
                imageView.visibility = View.VISIBLE
                placeHolderButton.visibility = View.GONE
                if (!image.isNew || fromServer) {
                    deleteButton.visibility = View.GONE
                } else {
                    deleteButton.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount() = imageItems.size
}

data class Image(
    val id: Int,
    val name: String,
    val type: ImageType,
    var path: String?,
    var remotePath: String?,
    var isNew: Boolean = true
) : Serializable

enum class ImageType(val value: String) {
    NORMAL("normal"),
    OTHER("other"),
}

interface ImageClickListener {
    fun onPlaceHolderClick(position: Int)
    fun onImageClick(image: Image)
    fun onDeleteClick(image: Image)
}
