package org.rfcx.incidents.view.report.create.image

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import org.rfcx.incidents.R
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

    private var reportImageAdapter: ReportImageAdapter? = null
    private var imageFile: File? = null
    private var filePath: String? = null

    private lateinit var attachImageDialog: BottomSheetDialog
    private val cameraPermissions by lazy { CameraPermissions(requireActivity()) }
    private val galleryPermissions by lazy { GalleryPermissions(requireActivity()) }

    private val CAMERA_IMAGE_PATH = "cameraImagePath"

    fun getReportImageAdapter(): ReportImageAdapter {
        if (reportImageAdapter != null) {
            return reportImageAdapter!!
        }
        reportImageAdapter = ReportImageAdapter()
        return reportImageAdapter!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupAttachImageDialog()
        setupReportImages()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (filePath != null) {
            outState.putString(CAMERA_IMAGE_PATH, filePath.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            if (it.containsKey(CAMERA_IMAGE_PATH)) {
                filePath = it.getString(CAMERA_IMAGE_PATH)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        handleTakePhotoResult(requestCode, resultCode)
        handleGalleryResult(requestCode, resultCode, data)
    }

    private fun setupReportImages() {
        getReportImageAdapter().onReportImageAdapterClickListener = object : OnReportImageAdapterClickListener {
            override fun onAddImageClick() {
                attachImageDialog.show()
            }

            override fun onDeleteImageClick(position: Int, imagePath: String) {
                getReportImageAdapter().removeAt(position)
                dismissImagePickerOptionsDialog()
                didRemoveImage(imagePath)
            }
        }

        getReportImageAdapter().setImages(arrayListOf())
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
            filePath = null
            cameraPermissions.check { }
        } else {
            startTakePhoto()
        }
    }

    private fun startTakePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = ReportUtils.createReportImageFile()
        filePath = imageFile.absolutePath
        val imageUri =
            context?.let {
                FileProvider.getUriForFile(
                    it,
                    ReportUtils.FILE_CONTENT_PROVIDER,
                    imageFile
                )
            }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(takePictureIntent, ReportUtils.REQUEST_TAKE_PHOTO)
    }

    private fun handleTakePhotoResult(requestCode: Int, resultCode: Int) {
        if (requestCode != ReportUtils.REQUEST_TAKE_PHOTO) return

        if (resultCode == Activity.RESULT_OK) {
            filePath?.let {
                val pathList = listOf(it)
                getReportImageAdapter().addImages(pathList)
                didAddImages(pathList)
            }
            dismissImagePickerOptionsDialog()
        } else {
            // remove file image
            filePath?.let {
                ImageFileUtils.removeFile(File(it))
                this.filePath = null
            }
        }
    }

    private fun openGallery() {
        if (!galleryPermissions.allowed()) {
            filePath = null
            galleryPermissions.check { }
        } else {
            startOpenGallery()
        }
    }

    private fun startOpenGallery() {
        val remainingImage = ReportImageAdapter.MAX_IMAGE_SIZE - getReportImageAdapter().getImageCount()
        Matisse.from(this)
            .choose(MimeType.ofImage())
            .countable(true)
            .maxSelectable(remainingImage)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f)
            .imageEngine(GlideEngine())
            .theme(R.style.Matisse_Dracula)
            .forResult(ReportUtils.REQUEST_GALLERY)
    }

    private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        val pathList = mutableListOf<String>()
        val results = Matisse.obtainResult(intentData)
        results.forEach {
            val imagePath = ImageFileUtils.findRealPath(requireContext(), it)
            imagePath?.let { path ->
                pathList.add(path)
            }
        }
        getReportImageAdapter().addImages(pathList)
        didAddImages(pathList)
        dismissImagePickerOptionsDialog()
    }
}
