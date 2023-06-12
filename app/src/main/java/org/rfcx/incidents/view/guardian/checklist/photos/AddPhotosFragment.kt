package org.rfcx.incidents.view.guardian.checklist.photos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianAddPhotosBinding
import org.rfcx.incidents.util.CameraPermissions
import org.rfcx.incidents.util.GalleryPermissions
import org.rfcx.incidents.util.ImageFileUtils
import org.rfcx.incidents.util.ImageUtils
import org.rfcx.incidents.util.socket.GuardianPlan
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.report.deployment.detail.image.AddImageListener
import java.io.File

class AddPhotosFragment : Fragment(), ImageClickListener, GuidelineButtonClickListener {

    private var imageAdapter: ImageAdapter? = null
    private var filePath: String? = null

    private val cameraPermissions by lazy { CameraPermissions(context as Activity) }
    private val galleryPermissions by lazy { GalleryPermissions(context as Activity) }

    private val CAMERA_IMAGE_PATH = "cameraImagePath"

    private var imagePlaceHolders = listOf<String>()
    private var imageGuidelineTexts = listOf<String>()
    private var imageExamples = listOf<String>()

    private lateinit var binding: FragmentGuardianAddPhotosBinding
    private val viewModel: AddPhotosViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null
    private var detailEvent: AddImageListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (viewModel.guardianPlan) {
            GuardianPlan.SAT_ONLY -> {
                imagePlaceHolders =
                    context.resources.getStringArray(R.array.sat_guardian_placeholders)
                        .toList()
                imageGuidelineTexts =
                    context.resources.getStringArray(R.array.sat_guardian_guideline_texts)
                        .toList()
                imageExamples =
                    context.resources.getStringArray(R.array.sat_guardian_photos).toList()
            }
            else -> {
                imagePlaceHolders =
                    context.resources.getStringArray(R.array.cell_guardian_placeholders)
                        .toList()
                imageGuidelineTexts =
                    context.resources.getStringArray(R.array.cell_guardian_guideline_texts)
                        .toList()
                imageExamples =
                    context.resources.getStringArray(R.array.cell_guardian_photos).toList()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        when(context) {
            is GuardianDeploymentEventListener -> mainEvent = context as GuardianDeploymentEventListener
            is AddImageListener -> detailEvent = context as AddImageListener
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_add_photos, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleTakePhotoResult(requestCode, resultCode)
        handleChooseImage(requestCode, resultCode, data)
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

        mainEvent?.let {
            it.showToolbar()
            it.hideThreeDots()
            it.setToolbarTitle(getString(R.string.photo_title))
        }

        setupImages()
        setupImageRecycler()
        updatePhotoTakenNumber()

        binding.finishButton.setOnClickListener {
            val missing = getImageAdapter().getMissingImages()
            if (missing.isEmpty()) {
                handleNextStep()
            } else {
                showFinishDialog(missing)
            }
        }
    }

    private fun getImageAdapter(): ImageAdapter {
        if (imageAdapter != null) {
            return imageAdapter!!
        }
        imageAdapter = ImageAdapter(this, imageExamples)
        return imageAdapter!!
    }

    private fun setupImages() {
        val savedImages = mainEvent?.getSavedImages()
        getImageAdapter().setPlaceHolders(imagePlaceHolders)
        if (!savedImages.isNullOrEmpty()) {
            getImageAdapter().updateImagesFromSavedImages(savedImages)
        }
    }

    private fun setupImageRecycler() {
        binding.attachImageRecycler.apply {
            adapter = getImageAdapter()
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun handleTakePhotoResult(requestCode: Int, resultCode: Int) {
        if (requestCode != ImageUtils.REQUEST_TAKE_PHOTO) return

        if (resultCode == Activity.RESULT_OK) {
            filePath?.let {
                getImageAdapter().updateTakeOrChooseImage(it)
                updatePhotoTakenNumber()
            }
        } else {
            // remove file image
            filePath?.let {
                ImageFileUtils.removeFile(File(it))
                this.filePath = null
            }
        }
    }

    private fun handleChooseImage(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ImageUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        intentData.data?.also {
            val path = ImageUtils.createImageFile(it, requireContext())
            if (path != null) {
                getImageAdapter().updateTakeOrChooseImage(path)
            }
        }
        updatePhotoTakenNumber()
    }

    private fun setCacheImages() {
        val images = getImageAdapter().getCurrentImagePaths()
        mainEvent?.setSavedImages(images)
    }

    private fun updatePhotoTakenNumber() {
        val number = getImageAdapter().getExistingImages().size
        binding.photoTakenTextView.text =
            getString(R.string.photo_taken, number, getImageAdapter().itemCount)
    }

    override fun onPlaceHolderClick(position: Int) {
        showGuidelineDialog(position)
    }

    override fun onDeleteClick(image: Image) {
        getImageAdapter().removeImage(image)
        updatePhotoTakenNumber()
    }

    override fun onTakePhotoClick() {
        openTakePhoto()
    }

    override fun onChoosePhotoClick() {
        openPhotoPicker()
    }

    private fun startTakePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = ImageUtils.createImageFile()
        filePath = imageFile.absolutePath
        val imageUri =
            context?.let {
                FileProvider.getUriForFile(
                    it,
                    ImageUtils.FILE_CONTENT_PROVIDER,
                    imageFile
                )
            }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(takePictureIntent, ImageUtils.REQUEST_TAKE_PHOTO)
    }

    private fun openTakePhoto() {
        if (checkPermission()) startTakePhoto()
    }

    private fun openPhotoPicker() {
        if (checkPermission()) startOpenPhotoPicker()
    }

    private fun checkPermission(): Boolean {
        return if (!cameraPermissions.allowed() || !galleryPermissions.allowed()) {
            filePath = null
            if (!cameraPermissions.allowed()) cameraPermissions.check { }
            if (!galleryPermissions.allowed()) galleryPermissions.check { }
            false
        } else {
            true
        }
    }

    private fun startOpenPhotoPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, ImageUtils.REQUEST_GALLERY)
    }

    private fun showGuidelineDialog(position: Int) {
        val guidelineDialog: PhotoGuidelineDialogFragment =
            this.parentFragmentManager.findFragmentByTag(PhotoGuidelineDialogFragment::class.java.name) as PhotoGuidelineDialogFragment?
                ?: run {
                    PhotoGuidelineDialogFragment.newInstance(
                        this,
                        imageGuidelineTexts.getOrNull(position)
                            ?: getString(R.string.take_other),
                        imageExamples.getOrNull(position) ?: "other"
                    )
                }
        if (guidelineDialog.isVisible || guidelineDialog.isAdded) return
        guidelineDialog.show(
            this.parentFragmentManager,
            PhotoGuidelineDialogFragment::class.java.name
        )
    }

    private fun showFinishDialog(missing: List<Image>) {
        MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
            setTitle(context.getString(R.string.missing_dialog_title))
            setMessage(
                context.getString(
                    R.string.follow_missing, missing.joinToString("\n") { "${it.id}. ${it.name}" }
                )
            )
            setPositiveButton(R.string.back) { _, _ -> }
            setNegativeButton(R.string.button_continue) { _, _ ->
                handleNextStep()
            }
        }.create().show()
    }

    private fun handleNextStep() {
        setCacheImages()
        mainEvent?.next()
    }

    companion object {
        fun newInstance(): AddPhotosFragment {
            return AddPhotosFragment()
        }
    }
}
