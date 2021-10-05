package org.rfcx.ranger.view.report.create

import android.content.Context
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_assets.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.RecordingPermissions
import org.rfcx.ranger.view.report.create.image.BaseImageFragment
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException

class AssetsFragment : BaseImageFragment() {
	
	companion object {
		@JvmStatic
		fun newInstance() = AssetsFragment()
	}
	
	lateinit var listener: CreateReportListener
	private var recordFile: File? = null
	private var recorder: MediaRecorder? = null
	private var player: MediaPlayer? = null
	
	private val recordPermissions by lazy { RecordingPermissions(requireActivity()) }
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun didAddImages(imagePaths: List<String>) {}
	
	override fun didRemoveImage(imagePath: String) {}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_assets, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupImageRecycler()
		view.viewTreeObserver.addOnGlobalLayoutListener { setOnFocusEditText() }
		
		saveDraftButton.setOnClickListener {
			saveAssets()
			listener.onSaveDraftButtonClick()
		}
		
		submitButton.setOnClickListener {
			saveAssets()
			listener.onSubmitButtonClick()
		}
		
		setTextChanged()
		setOnCheckedChange()
		setupAssets()
		setupRecordSoundProgressView()
		checkIsEnabledButton()
	}
	
	private fun setTextChanged() {
		noteEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(p0: Editable?) {
				checkIsEnabledButton()
			}
			
			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
		})
	}
	
	private fun setOnCheckedChange() {
		noneCollectedCheckBox.setOnCheckedChangeListener { _, _ ->
			checkIsEnabledButton()
		}
	}
	
	private fun setupAssets() {
		val response = listener.getResponse()
		response?.note?.let { note -> noteEditText.setText(note) }
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
		noteEditText.text?.let {
			listener.setNotes(it.toString())
		}
		listener.setImages(reportImageAdapter.getNewAttachImage())
		listener.setAudio(recordFile?.canonicalPath)
	}
	
	private fun setupImageRecycler() {
		reportImageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
			override fun onChanged() {
				checkIsEnabledButton()
			}
		})
		attachImageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		reportImageAdapter.setImages(arrayListOf())
	}
	
	private fun setOnFocusEditText() {
		val screenHeight: Int = view?.rootView?.height ?: 0
		val r = Rect()
		view?.getWindowVisibleDisplayFrame(r)
		val keypadHeight: Int = screenHeight - r.bottom
		if (keypadHeight > screenHeight * 0.15) {
			saveDraftButton.visibility = View.GONE
			submitButton.visibility = View.GONE
		} else {
			if (saveDraftButton != null) {
				saveDraftButton.visibility = View.VISIBLE
			}
			if (submitButton != null) {
				submitButton.visibility = View.VISIBLE
			}
		}
	}
	
	private fun checkIsEnabledButton() {
		when {
			reportImageAdapter.getNewAttachImage().isNotEmpty() -> {
				setEnabled(true)
			}
			!TextUtils.isEmpty(noteEditText.text) -> {
				setEnabled(true)
			}
			recordFile?.canonicalPath != null -> {
				setEnabled(true)
			}
			noneCollectedCheckBox.isChecked -> {
				setEnabled(true)
			}
			else -> {
				setEnabled(false)
			}
		}
	}
	
	private fun setEnabled(isEnabled: Boolean) {
		saveDraftButton.isEnabled = isEnabled
		submitButton.isEnabled = isEnabled
	}
	
	private fun setAudio(path: String) {
		recordFile = File(path)
		
		if (recordFile?.exists() == true) {
			soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
		}
	}
	
	private fun setupRecordSoundProgressView() {
		soundRecordProgressView.onStateChangeListener = { state ->
			when (state) {
				SoundRecordState.NONE -> {
					recordFile?.deleteOnExit()
					recordFile = null
					checkIsEnabledButton()
				}
				SoundRecordState.RECORDING -> {
					record()
				}
				SoundRecordState.STOPPED_RECORD -> {
					stopRecording()
					checkIsEnabledButton()
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
				soundRecordProgressView.state = SoundRecordState.NONE
				Snackbar.make(assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
			}
		}
		recorder = null
	}
	
	private fun record() {
		if (!recordPermissions.allowed()) {
			soundRecordProgressView.state = SoundRecordState.NONE
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
					soundRecordProgressView.state = SoundRecordState.NONE
					Snackbar.make(assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				}
				start()
			}
		}
	}
	
	private fun startPlaying() {
		if (recordFile == null) {
			soundRecordProgressView.state = SoundRecordState.NONE
			return
		}
		player = MediaPlayer().apply {
			try {
				setDataSource(recordFile!!.absolutePath)
				prepare()
				start()
				setOnCompletionListener {
					soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
				}
			} catch (e: IOException) {
				soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
				Snackbar.make(assetsView, R.string.error_common, Snackbar.LENGTH_LONG).show()
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
