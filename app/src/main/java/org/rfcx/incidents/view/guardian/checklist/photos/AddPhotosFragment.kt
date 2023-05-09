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
import com.opensooq.supernova.gligar.GligarPicker
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianAddPhotosBinding
import org.rfcx.incidents.databinding.FragmentGuardianStorageBinding
import org.rfcx.incidents.util.CameraPermissions
import org.rfcx.incidents.util.GalleryPermissions
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.checklist.storage.GuardianStorageViewModel
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
    private val viewModel: GuardianStorageViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_storage, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleTakePhotoResult(requestCode, resultCode)
        handleGligarPickerResult(requestCode, resultCode, data)
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

        setupImages()
        setupImageRecycler()
        updatePhotoTakenNumber()

        binding.finishButton.setOnClickListener {
            val existing = getImageAdapter().getExistingImages()
            val missing = getImageAdapter().getMissingImages()
            if (missing.isEmpty()) {
                handleNextStep(existing)
            } else {
                showFinishDialog(existing, missing)
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

        val savedImages =
            audioMothDeploymentProtocol?.getImages() ?: songMeterDeploymentProtocol?.getImages()
                ?: guardianDeploymentProtocol?.getImages()
        getImageAdapter().setPlaceHolders(imagePlaceHolders)
        if (savedImages != null && savedImages.isNotEmpty()) {
            getImageAdapter().updateImagesFromSavedImages(savedImages)
        }
    }

    private fun setupImageRecycler() {
        attachImageRecycler.apply {
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

    private fun handleGligarPickerResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        if (requestCode != ImageUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return

        val results = intentData.extras?.getStringArray(GligarPicker.IMAGES_RESULT)
        results?.forEach {
            getImageAdapter().updateTakeOrChooseImage(it)
        }
        updatePhotoTakenNumber()
    }

    private fun setCacheImages() {
        val images = getImageAdapter().getCurrentImagePaths()
        audioMothDeploymentProtocol?.setImages(images)
        songMeterDeploymentProtocol?.setImages(images)
        guardianDeploymentProtocol?.setImages(images)
    }

    private fun updatePhotoTakenNumber() {
        val number = getImageAdapter().getExistingImages().size
        photoTakenTextView.text =
            getString(R.string.photo_taken, number, getImageAdapter().itemCount)
    }

    override fun onPlaceHolderClick(position: Int) {
        showGuidelineDialog(position)
    }

    override fun onImageClick(image: Image) {
        if (image.path == null) return
        context?.let { DisplayImageActivity.startActivity(it, arrayOf("file://${image.path}"), arrayOf(image.name)) }
    }

    override fun onDeleteClick(image: Image) {
        getImageAdapter().removeImage(image)
        updatePhotoTakenNumber()
    }

    override fun onTakePhotoClick() {
        openTakePhoto()
    }

    override fun onChoosePhotoClick() {
        openGligarPicker()
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

    private fun openGligarPicker() {
        if (checkPermission()) startOpenGligarPicker()
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

    private fun startOpenGligarPicker() {
        GligarPicker()
            .requestCode(ImageUtils.REQUEST_GALLERY)
            .limit(1)
            .withFragment(this)
            .disableCamera(true)
            .show()
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

    private fun showFinishDialog(existing: List<Image>, missing: List<Image>) {
        MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
            setTitle(context.getString(R.string.missing_dialog_title))
            setMessage(
                context.getString(
                    R.string.follow_missing, missing.joinToString("\n") { "${it.id}. ${it.name}" }
                )
            )
            setPositiveButton(R.string.back) { _, _ -> }
            setNegativeButton(R.string.button_continue) { _, _ ->
                handleNextStep(existing)
            }
        }.create().show()
    }

    private fun handleNextStep(images: List<Image>) {
        setCacheImages()
        when (screen) {
            Screen.AUDIO_MOTH_CHECK_LIST.id -> {
                if (images.isNotEmpty()) {
                    analytics?.trackAddDeploymentImageEvent(Device.AUDIOMOTH.value)
                }
                audioMothDeploymentProtocol?.nextStep()
            }
            Screen.SONG_METER_CHECK_LIST.id -> {
                if (images.isNotEmpty()) {
                    analytics?.trackAddDeploymentImageEvent(Device.SONGMETER.value)
                }
                songMeterDeploymentProtocol?.nextStep()
            }
            Screen.GUARDIAN_CHECK_LIST.id -> {
                if (images.isNotEmpty()) {
                    analytics?.trackAddDeploymentImageEvent(Device.GUARDIAN.value)
                }
                guardianDeploymentProtocol?.nextStep()
            }
        }
    }

    companion object {
        fun newInstance(): AddPhotosFragment {
            return AddPhotosFragment()
        }
    }
}
