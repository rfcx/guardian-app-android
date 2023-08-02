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
import org.rfcx.incidents.util.ImageUtils
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

    private val cameraPermissions by lazy { CameraPermissions(requireActivity()) }
    private val galleryPermissions by lazy { GalleryPermissions(requireActivity()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupReportImages()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        handleChooseImage(requestCode, resultCode, data)
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
                didRemoveImage(imagePath)
            }
        }

        reportImageAdapter.setImages(arrayListOf())
    }

    private fun startOpenGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, ImageUtils.REQUEST_GALLERY)
    }

    private fun handleChooseImage(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ImageUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        intentData.data?.also {
            val path = ImageUtils.createImageFile(it, requireContext())
            if (path != null) {
                reportImageAdapter.addImages(listOf(path))
            }
        }
    }
}
