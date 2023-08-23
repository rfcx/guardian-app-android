package org.rfcx.incidents.view.report.detail

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentResponseDetailBinding
import org.rfcx.incidents.entity.response.LoggingScale
import org.rfcx.incidents.entity.response.PoachingScale
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.FeatureCollection
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.base.BaseMapFragment
import org.rfcx.incidents.view.report.create.image.ReportImageAdapter
import org.rfcx.incidents.widget.SoundRecordState
import java.io.File
import java.io.IOException
import java.util.TimeZone

class ResponseDetailFragment : BaseMapFragment() {

    lateinit var binding: FragmentResponseDetailBinding

    private val viewModel: ResponseDetailViewModel by viewModel()
    private val reportImageAdapter by lazy { ReportImageAdapter() }

    private var responseCoreId: String? = null
    private var response: Response? = null
    private var recordFile: File? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIntent()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_response_detail, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }

    private fun initIntent() {
        arguments?.let {
            responseCoreId = it.getString(RESPONSE_CORE_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapView!!.getMapAsync(this)

        response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
        setupRecordSoundProgressView()
        setupView()

        binding.attachImageRecycler.apply {
            adapter = reportImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        setGoogleMap(p0, false)
        setTrack()
    }

    private fun setTrack() {
        response?.let { res ->
            val track = res.trackingAssets.firstOrNull()
            if (track != null) {
                val json = File(track.localPath).readText()
                val featureCollection = Gson().fromJson(json, FeatureCollection::class.java)

                val latLngList = mutableListOf<LatLng>()
                featureCollection.features.forEach {
                    it.geometry.coordinates.forEach { c ->
                        latLngList.add(LatLng(c[1], c[0]))
                    }
                }
                setPolyline(latLngList, featureCollection.features[0].properties.color)
            } else {
                binding.mapCardView.visibility = View.GONE
            }
        }
    }

    private fun setupView() {
        response?.let { res ->
            val timeZone = TimeZone.getTimeZone(viewModel.getStream(res.streamId)?.timezoneRaw)
            binding.investigatedAtValueTextView.text =
                if (timeZone == TimeZone.getDefault()) res.investigatedAt.toTimeSinceStringAlternativeTimeAgo(requireContext(), timeZone)
                else res.investigatedAt.toStringWithTimeZone(requireContext(), timeZone)

            binding.receivedValueTextView.text =
                if (timeZone == TimeZone.getDefault()) res.submittedAt?.toTimeSinceStringAlternativeTimeAgo(requireContext(), timeZone)
                else res.submittedAt?.toStringWithTimeZone(requireContext(), timeZone)

            binding.loggingValueTextView.text = getMessageList(res.answers, '1')
            binding.poachingValueTextView.text = getMessageList(res.answers, '6')
            binding.actionValueTextView.text = getMessageList(res.answers, '2')

            binding.loggingLayout.visibility = if (res.answers.none { it.toString()[0] == '1' }) View.GONE else View.VISIBLE
            binding.poachingLayout.visibility = if (res.answers.none { it.toString()[0] == '6' }) View.GONE else View.VISIBLE
            binding.actionLayout.visibility = if (res.answers.none { it.toString()[0] == '2' }) View.GONE else View.VISIBLE

            binding.scaleLoggingTextView.text = setScale(res.answers.filter { it.toString()[0] == '3' })
            binding.scaleLoggingTextView.visibility = if (res.answers.contains(LoggingScale.NONE.value)) View.GONE else View.VISIBLE

            binding.scalePoachingTextView.text = setScale(res.answers.filter { it.toString()[0] == '7' })
            binding.scalePoachingTextView.visibility = if (res.answers.contains(PoachingScale.NONE.value)) View.GONE else View.VISIBLE

            binding.noteTextView.visibility = if (res.note != null) View.VISIBLE else View.GONE
            binding.noteTextView.text = res.note
            if (res.audioAssets.isNotEmpty()) setAudio(res.audioAssets[0].localPath)
            binding.soundRecordProgressView.disableEdit()
            binding.soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
            binding.soundRecordProgressView.visibility = if (res.audioAssets.isNotEmpty()) View.VISIBLE else View.GONE
            res.guid?.let {
                binding.attachImageRecycler.visibility = if (res.imageAssets.isNotEmpty()) View.VISIBLE else View.GONE
                reportImageAdapter.setImages(res.imageAssets, false)
                binding.additionalEvidenceLayout.visibility =
                    if (res.note == null && res.imageAssets.isEmpty() && res.audioLocation == null) View.GONE else View.VISIBLE
            }
        }
    }

    private fun setupRecordSoundProgressView() {
        binding.soundRecordProgressView.onStateChangeListener = { state ->
            when (state) {
                SoundRecordState.NONE -> {
                    recordFile?.deleteOnExit()
                    recordFile = null
                }

                SoundRecordState.PLAYING -> {
                    startPlaying()
                }

                SoundRecordState.STOP_PLAYING -> {
                    stopPlaying()
                }

                SoundRecordState.RECORDING -> {
                }

                SoundRecordState.STOPPED_RECORD -> {
                }
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
                Snackbar.make(binding.guardianListScrollView, R.string.error_common, Snackbar.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun getMessageList(answers: List<Int>, num: Char): String {
        var message = ""
        answers.forEach {
            if (it.toString()[0] == num) {
                message += if (message.isBlank()) {
                    it.getAnswerItem(requireContext())
                } else {
                    ", " + it.getAnswerItem(requireContext())
                }
            }
        }
        return message
    }

    private fun setAudio(path: String) {
        recordFile = File(path)

        if (recordFile?.exists() == true) {
            binding.soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
        }
    }

    private fun setScale(answers: List<Int>): String {
        if (answers.contains(LoggingScale.LARGE.value) || answers.contains(PoachingScale.LARGE.value))
            return getString(R.string.large_scale_text)

        if (answers.contains(LoggingScale.SMALL.value) || answers.contains(PoachingScale.SMALL.value))
            return getString(R.string.small_scale_text)

        return ""
    }

    companion object {
        private const val RESPONSE_CORE_ID = "RESPONSE_CORE_ID"

        @JvmStatic
        fun newInstance(responseCoreId: String) = ResponseDetailFragment().apply {
            arguments = Bundle().apply {
                putString(RESPONSE_CORE_ID, responseCoreId)
            }
        }
    }
}
