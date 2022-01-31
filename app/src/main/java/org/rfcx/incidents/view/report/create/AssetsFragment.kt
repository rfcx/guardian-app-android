package org.rfcx.incidents.view.report.create

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentAssetsBinding
import org.rfcx.incidents.entity.response.Actions
import org.rfcx.incidents.entity.response.EvidenceTypes
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.RecordingPermissions
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.hideKeyboard
import org.rfcx.incidents.view.report.create.image.BaseImageFragment
import org.rfcx.incidents.widget.SoundRecordState
import java.io.File
import java.io.IOException

class AssetsFragment : BaseImageFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AssetsFragment()
    }

    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!

    lateinit var listener: CreateReportListener
    private var recordFile: File? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private val recordPermissions by lazy { RecordingPermissions(requireActivity()) }
    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.ASSETS)
    }

    override fun didAddImages(imagePaths: List<String>) {}

    override fun didRemoveImage(imagePath: String) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageRecycler()
        view.viewTreeObserver.addOnGlobalLayoutListener { setOnFocusEditText() }

        binding.saveDraftButton.setOnClickListener {
            saveAssets()
            analytics?.trackSaveDraftResponseEvent()
            listener.onSaveDraftButtonClick()
        }

        binding.submitButton.setOnClickListener {
            if (!TextUtils.isEmpty(binding.noteEditText.text) || recordFile?.canonicalPath != null || reportImageAdapter.getNewAttachImage()
                    .isNotEmpty()
            ) {
                saveAssets()
                analytics?.trackSubmitResponseEvent()
                listener.onSubmitButtonClick()
            } else {
                showDefaultDialog()
            }
        }

        binding.noteEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }

        setupAssets()
        setupRecordSoundProgressView()
        setRequiredNote()

        view.setOnClickListener {
            it.hideKeyboard()
        }
    }

    private fun setRequiredNote() {
        val res = listener.getResponse()
        res?.let { response ->
            if (response.evidences.contains(EvidenceTypes.NONE.value) || response.responseActions.contains(Actions.OTHER.value)) {
                binding.submitButton.isEnabled = binding.noteEditText.text?.isNotBlank() ?: false

                val spannableString = SpannableString(getString(R.string.add_notes_required))
                val red = ForegroundColorSpan(Color.RED)
                spannableString.setSpan(
                    red,
                    getString(R.string.add_notes_required).length - 1,
                    getString(R.string.add_notes_required).length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.noteTextView.text = spannableString

                binding.noteEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        binding.submitButton.isEnabled = !s.isNullOrBlank()
                    }
                })
            }
        }
    }

    private fun setOnFocusEditText() {
        val screenHeight: Int = view?.rootView?.height ?: 0
        val r = Rect()
        view?.getWindowVisibleDisplayFrame(r)
        val keypadHeight: Int = screenHeight - r.bottom
        if (keypadHeight > screenHeight * 0.15) {
            binding.saveDraftButton.visibility = View.GONE
            binding.submitButton.visibility = View.GONE
        } else {
            binding.saveDraftButton.visibility = View.VISIBLE
            binding.submitButton.visibility = View.VISIBLE
        }
    }

    private fun showDefaultDialog() {
        context?.let {
            val dialog = MaterialAlertDialogBuilder(it)
                .setTitle(R.string.submit_report)
                .setMessage(resources.getString(R.string.are_you_sure))
                .setNegativeButton(resources.getString(R.string.report_submit_button_label)) { _, _ ->
                    saveAssets()
                    analytics?.trackSubmitResponseEvent()
                    listener.onSubmitButtonClick()
                }
                .setPositiveButton(resources.getString(R.string.cancel)) { _, _ -> }
                .show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }
    }

    private fun setupAssets() {
        val response = listener.getResponse()
        response?.note?.let { note -> binding.noteEditText.setText(note) }
        response?.audioLocation?.let { path -> setAudio(path) }

        val images = listener.getImages()
        if (images.isNotEmpty()) {
            val pathList = mutableListOf<String>()
            images.forEach {
                pathList.add(it)
            }
            reportImageAdapter.addImages(pathList)
            didAddImages(pathList)
        }
    }

    private fun saveAssets() {
        binding.noteEditText.text?.let {
            if (it.isNotBlank()) {
                listener.setNotes(it.toString())
            }
        }
        listener.setImages(ArrayList(reportImageAdapter.getNewAttachImage()))
        listener.setAudio(recordFile?.canonicalPath)
    }

    private fun setupImageRecycler() {
        binding.attachImageRecycler.apply {
            adapter = reportImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        reportImageAdapter.setImages(arrayListOf())
    }

    private fun setAudio(path: String) {
        recordFile = File(path)

        if (recordFile?.exists() == true) {
            binding.soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
        }
    }

    private fun setupRecordSoundProgressView() {
        binding.soundRecordProgressView.onStateChangeListener = { state ->
            when (state) {
                SoundRecordState.NONE -> {
                    recordFile?.deleteOnExit()
                    recordFile = null
                }
                SoundRecordState.RECORDING -> {
                    view?.hideKeyboard()
                    record()
                }
                SoundRecordState.STOPPED_RECORD -> {
                    stopRecording()
                }
                SoundRecordState.PLAYING -> {
                    startPlaying()
                }
                SoundRecordState.STOP_PLAYING -> {
                    stopPlaying()
                }
            }
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
                binding.soundRecordProgressView.state = SoundRecordState.NONE
                Snackbar.make(binding.assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
            }
        }
        recorder = null
    }

    private fun record() {
        if (!recordPermissions.allowed()) {
            binding.soundRecordProgressView.state = SoundRecordState.NONE
            recordPermissions.check { }
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        recordFile = File.createTempFile("Record${System.currentTimeMillis()}", ".mp3", requireActivity().cacheDir)
        recordFile?.let {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(recordFile!!.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                    binding.soundRecordProgressView.state = SoundRecordState.NONE
                    Snackbar.make(binding.assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
                }
                start()
            }
        }
    }

    private fun startPlaying() {
        if (recordFile == null) {
            binding.soundRecordProgressView.state = SoundRecordState.NONE
            return
        }
        player = MediaPlayer().apply {
            try {
                setDataSource(recordFile!!.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    binding.soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
                }
            } catch (e: IOException) {
                binding.soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
                Snackbar.make(binding.assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
    }
}
