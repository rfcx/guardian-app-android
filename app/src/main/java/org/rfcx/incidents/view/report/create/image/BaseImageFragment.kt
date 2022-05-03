package org.rfcx.incidents.view.report.create.image

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.opensooq.supernova.gligar.GligarPicker
import org.rfcx.incidents.databinding.ButtomSheetAttachImageLayoutBinding
import org.rfcx.incidents.util.CameraPermissions
import org.rfcx.incidents.util.GalleryPermissions
import org.rfcx.incidents.util.ImageFileUtils
import org.rfcx.incidents.util.ReportUtils
import org.rfcx.incidents.view.base.BaseFragment
import java.io.File

/**
 * Shared image functionality between ReportActivity and ReportDetailActivity
 */

abstract class BaseImageFragment : BaseFragment() {
    protected abstract fun didAddImages(imagePaths: List<String>)
    protected abstract fun didRemoveImage(imagePath: String)

    protected val reportImageAdapter by lazy { ReportImageAdapter() }
    private var imageFile: File? = null

    private lateinit var attachImageDialog: BottomSheetDialog
    private val cameraPermissions by lazy { CameraPermissions(requireActivity()) }
    private val galleryPermissions by lazy { GalleryPermissions(requireActivity()) }

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
                if (!cameraPermissions.allowed() || !galleryPermissions.allowed()) {
                    imageFile = null
                    if (!cameraPermissions.allowed()) cameraPermissions.check { }
                    if (!galleryPermissions.allowed()) galleryPermissions.check { }
                } else {
                    startOpenGallery()
                }
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
        val binding = ButtomSheetAttachImageLayoutBinding.inflate(LayoutInflater.from(context), null, false)
        binding.menuGallery.setOnClickListener {
            openGallery()
        }

        binding.menuTakePhoto.setOnClickListener {
            takePhoto()
        }
        attachImageDialog = BottomSheetDialog(requireContext())
        attachImageDialog.setContentView(binding.root)
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

        val image = imageFile ?: return
        val photoURI = FileProvider.getUriForFile(requireContext(), ReportUtils.FILE_CONTENT_PROVIDER, image)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, ReportUtils.REQUEST_TAKE_PHOTO)
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
        GligarPicker()
            .requestCode(ReportUtils.REQUEST_GALLERY)
            .limit(remainingImage)
            .withFragment(this)
            .show()
    }

    private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        val pathList = mutableListOf<String>()
        val results = intentData.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
        results?.forEach {
            pathList.add(it)
        }
        reportImageAdapter.addImages(pathList)
        didAddImages(pathList)
        dismissImagePickerOptionsDialog()
    }
}
