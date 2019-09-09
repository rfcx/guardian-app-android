package org.rfcx.ranger.view.report

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.buttom_sheet_attach_image_layout.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnReportImageAdapterClickListener
import org.rfcx.ranger.adapter.ReportImageAdapter
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.base.BaseActivity
import java.io.File

/**
 * Shared image functionality between ReportActivity and ReportDetailActivity
 */

abstract class BaseReportImageActivity: BaseActivity() {
	
	protected abstract fun didAddImages(imagePaths: List<String>)
	protected abstract fun didRemoveImage(imagePath: String)
	
	protected val reportImageAdapter by lazy { ReportImageAdapter() }
	private var imageFile: File? = null
	
	private lateinit var attachImageDialog: BottomSheetDialog
	private val cameraPermissions by lazy { CameraPermissions(this) }
	private val galleryPermissions by lazy { GalleryPermissions(this) }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setupAttachImageDialog()
		setupReportImages()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		
		handleTakePhotoResult(requestCode, resultCode)
		handleGalleryResult(requestCode, resultCode, data)
	}
	
	private fun setupReportImages() {
		reportImageAdapter.onReportImageAdapterClickListener = object : OnReportImageAdapterClickListener {
			override fun onAddImageClick() {
				attachImageDialog.show()
			}
			
			override fun onDeleteImageClick(position: Int, imagePath: String) {
				reportImageAdapter.removeAt(position)
				dismissImagePickerOptionsDialog()
				didRemoveImage(imagePath)
			}
		}
		
		reportImageAdapter.setImages(arrayListOf())
		dismissImagePickerOptionsDialog()
	}
	
	private fun setupAttachImageDialog() {
		val bottomSheetView = layoutInflater.inflate(R.layout.buttom_sheet_attach_image_layout, null)
		
		bottomSheetView.menuGallery.setOnClickListener {
			openGallery()
		}
		
		bottomSheetView.menuTakePhoto.setOnClickListener {
			takePhoto()
		}
		
		attachImageDialog = BottomSheetDialog(this)
		attachImageDialog.setContentView(bottomSheetView)
	}
	
	private fun dismissImagePickerOptionsDialog() {
		attachImageDialog.dismiss()
	}
	
	private fun takePhoto() {
		if (!cameraPermissions.allowed()) {
			imageFile = null
			cameraPermissions.check { }
		} else {
			startTakePhoto()
		}
	}
	
	private fun startTakePhoto() {
		val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		imageFile = ReportUtils.createReportImageFile()
		if (imageFile != null) {
			val photoURI = FileProvider.getUriForFile(this, ReportUtils.FILE_CONTENT_PROVIDER, imageFile!!)
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
			startActivityForResult(takePictureIntent, ReportUtils.REQUEST_TAKE_PHOTO)
		} else {
			// TODO: handle on can't create image file
		}
	}
	
	private fun handleTakePhotoResult(requestCode: Int, resultCode: Int) {
		if (requestCode != ReportUtils.REQUEST_TAKE_PHOTO) return
		
		if (resultCode == Activity.RESULT_OK) {
			imageFile?.let {
				val pathList = listOf(it.absolutePath)
				reportImageAdapter.addImages(pathList)
				didAddImages(pathList)
			}
			dismissImagePickerOptionsDialog()
			
		} else {
			// remove file image
			imageFile?.let {
				ImageFileUtils.removeFile(it)
				this.imageFile = null
			}
		}
	}
	
	private fun openGallery() {
		if (!galleryPermissions.allowed()) {
			imageFile = null
			galleryPermissions.check { }
		} else {
			startOpenGallery()
		}
	}
	
	private fun startOpenGallery() {
		val remainingImage = ReportImageAdapter.MAX_IMAGE_SIZE - reportImageAdapter.getImageCount()
		Matisse.from(this)
				.choose(MimeType.ofImage())
				.countable(true)
				.maxSelectable(remainingImage)
				.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				.thumbnailScale(0.85f)
				.imageEngine(GlideV4ImageEngine())
				.theme(R.style.Matisse_Dracula)
				.forResult(ReportUtils.REQUEST_GALLERY)
	}
	
	private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return
		
		val pathList = mutableListOf<String>()
		val results = Matisse.obtainResult(intentData)
		results.forEach {
			val imagePath = ImageFileUtils.findRealPath(this, it)
			imagePath?.let { path ->
				pathList.add(path)
			}
		}
		reportImageAdapter.addImages(pathList)
		didAddImages(pathList)
		dismissImagePickerOptionsDialog()
	}
}