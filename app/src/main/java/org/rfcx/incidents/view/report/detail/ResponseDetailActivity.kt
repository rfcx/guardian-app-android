package org.rfcx.incidents.view.report.detail

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ActivityResponseDetailBinding
import org.rfcx.incidents.entity.response.LoggingScale
import org.rfcx.incidents.entity.response.PoachingScale
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.report.create.image.ReportImageAdapter
import org.rfcx.incidents.widget.SoundRecordState
import java.io.File
import java.io.IOException
import java.util.TimeZone

class ResponseDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESPONSE_CORE_ID = "EXTRA_RESPONSE_CORE_ID"
        private const val SOURCE_LINE = "source.line"
        private const val SOURCE_CHECK_IN = "source.checkin"
        private const val MARKER_CHECK_IN_ID = "marker.checkin"
        private const val MARKER_CHECK_IN_IMAGE = "marker.checkin.pin"

        fun startActivity(context: Context, responseCoreId: String) {
            val intent = Intent(context, ResponseDetailActivity::class.java)
            intent.putExtra(EXTRA_RESPONSE_CORE_ID, responseCoreId)
            context.startActivity(intent)
        }
    }

    lateinit var binding: ActivityResponseDetailBinding

    private val analytics by lazy { Analytics(this) }
    private val viewModel: ResponseDetailViewModel by viewModel()
    private val reportImageAdapter by lazy { ReportImageAdapter() }

    private var responseCoreId: String? = null
    private var response: Response? = null
    private var recordFile: File? = null
    private var player: MediaPlayer? = null

    // private lateinit var mapView: MapView
    // private lateinit var mapBoxMap: MapboxMap
    // private var lineSource: GeoJsonSource? = null
    // private var checkInSource: GeoJsonSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResponseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
        response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
        setupToolbar()
        setupRecordSoundProgressView()

        binding.attachImageRecycler.apply {
            adapter = reportImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        // Setup Mapbox
        // mapView = findViewById(R.id.mapBoxView)
        // mapView.onCreate(savedInstanceState)
        // mapView.getMapAsync(this)

        response?.let { res ->
            val timeZone = TimeZone.getTimeZone(viewModel.getStream(res.streamId)?.timezoneRaw)
            binding.investigatedAtValueTextView.text =
                if (timeZone == TimeZone.getDefault()) res.investigatedAt.toTimeSinceStringAlternativeTimeAgo(this, timeZone)
                else res.investigatedAt.toStringWithTimeZone(this, timeZone)

            binding.receivedValueTextView.text =
                if (timeZone == TimeZone.getDefault()) res.submittedAt?.toTimeSinceStringAlternativeTimeAgo(this, timeZone)
                else res.submittedAt?.toStringWithTimeZone(this, timeZone)

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

    private fun getMessageList(answers: List<Int>, num: Char): String {
        var message = ""
        answers.forEach {
            if (it.toString()[0] == num) {
                message += if (message.isBlank()) {
                    it.getAnswerItem(this)
                } else {
                    ", " + it.getAnswerItem(this)
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

    // override fun onMapReady(mapboxMap: MapboxMap) {
    //     mapBoxMap = mapboxMap
    //     mapboxMap.uiSettings.apply {
    //         setAllGesturesEnabled(false)
    //         isAttributionEnabled = false
    //         isLogoEnabled = false
    //     }
    //
    //     mapboxMap.setStyle(Style.OUTDOORS) { style ->
    //         setupSources(style)
    //
    //         response?.let { res ->
    //             val track = res.trackingAssets.firstOrNull()
    //             if (track != null) {
    //                 val tempTrack = arrayListOf<Feature>()
    //                 val json = File(track.localPath).readText()
    //                 val featureCollection = FeatureCollection.fromJson(json)
    //                 val feature = featureCollection.features()?.get(0)
    //                 feature?.let {
    //                     tempTrack.add(it)
    //                 }
    //                 addLineLayer(style)
    //                 lineSource?.setGeoJson(FeatureCollection.fromFeatures(tempTrack))
    //
    //                 val lastLocation = feature?.geometry() as LineString
    //                 val pointFeatures = lastLocation.coordinates().map {
    //                     Feature.fromGeometry(Point.fromLngLat(it.longitude(), it.latitude()))
    //                 }
    //                 checkInSource?.setGeoJson(FeatureCollection.fromFeatures(pointFeatures))
    //                 moveCameraToLeavesBounds(lastLocation.coordinates())
    //             } else {
    //                 binding.mapBoxCardView.visibility = View.GONE
    //             }
    //         }
    //     }
    // }

    // private fun moveCameraToLeavesBounds(features: List<Point>) {
    //     val latLngList: ArrayList<LatLng> = ArrayList()
    //     for (singleClusterFeature in features) {
    //         latLngList.add(LatLng(singleClusterFeature.latitude(), singleClusterFeature.longitude()))
    //     }
    //     if (latLngList.size > 1) {
    //         moveCameraWithLatLngList(latLngList)
    //     } else {
    //         moveCamera(latLngList[0])
    //     }
    // }

    // private fun moveCameraWithLatLngList(latLngList: List<LatLng>) {
    //     val latLngBounds = LatLngBounds.Builder()
    //         .includes(latLngList)
    //         .build()
    //     mapBoxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 230), 1300)
    // }

    // private fun moveCamera(loc: LatLng) {
    //     mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10.0))
    // }

    // private fun setupSources(style: Style) {
    //     lineSource = GeoJsonSource(SOURCE_LINE)
    //     lineSource?.let { style.addSource(it) }
    //
    //     checkInSource = GeoJsonSource(SOURCE_CHECK_IN)
    //     checkInSource?.let { style.addSource(it) }
    // }

    // private fun addLineLayer(style: Style) {
    //     val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
    //         PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
    //         PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
    //         PropertyFactory.lineWidth(5f),
    //         PropertyFactory.lineColor(Expression.get("color"))
    //     )
    //     style.addLayer(lineLayer)
    //
    //     val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_circle_tracking, null)
    //     val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
    //     mBitmap?.let { style.addImage(MARKER_CHECK_IN_IMAGE, it) }
    //
    //     val checkInLayer = SymbolLayer(MARKER_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
    //         withProperties(
    //             PropertyFactory.iconImage(MARKER_CHECK_IN_IMAGE),
    //             PropertyFactory.iconAllowOverlap(true),
    //             PropertyFactory.iconIgnorePlacement(true),
    //             PropertyFactory.iconSize(1f)
    //         )
    //     }
    //     style.addLayer(checkInLayer)
    // }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = "#" + response?.incidentRef + " " + response?.streamName
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.trackScreen(Screen.RESPONSE_DETAIL)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
