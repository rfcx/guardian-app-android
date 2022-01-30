package org.rfcx.incidents.view.report.detail

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_response_detail.*
import kotlinx.android.synthetic.main.activity_response_detail.attachImageRecycler
import kotlinx.android.synthetic.main.activity_response_detail.noteTextView
import kotlinx.android.synthetic.main.activity_response_detail.soundRecordProgressView
import kotlinx.android.synthetic.main.fragment_assets.*
import kotlinx.android.synthetic.main.widget_sound_record_progress.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.report.create.image.ReportImageAdapter
import org.rfcx.incidents.widget.SoundRecordState
import java.io.File
import java.io.IOException


class ResponseDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    
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
    
    private val analytics by lazy { Analytics(this) }
    private val viewModel: ResponseDetailViewModel by viewModel()
    private val responseDetailAdapter by lazy { ResponseDetailAdapter() }
    private val reportImageAdapter by lazy { ReportImageAdapter() }
    
    private var responseCoreId: String? = null
    private var response: Response? = null
    private var recordFile: File? = null
    private var player: MediaPlayer? = null
    
    private lateinit var mapView: MapView
    private lateinit var mapBoxMap: MapboxMap
    private var lineSource: GeoJsonSource? = null
    private var checkInSource: GeoJsonSource? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_token))
        setContentView(R.layout.activity_response_detail)
        responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
        response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
        setupToolbar()
        setupRecordSoundProgressView()
        
        answersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = responseDetailAdapter
        }
        
        attachImageRecycler.apply {
            adapter = reportImageAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        
        // Setup Mapbox
        mapView = findViewById(R.id.mapBoxView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        
        response?.let { res ->
            investigateAtTextView.text = res.investigatedAt.toTimeSinceStringAlternativeTimeAgo(this)
            responseDetailAdapter.items = getMessageList(res.answers)
            noteTextView.visibility = if (res.note != null) View.VISIBLE else View.GONE
            noteTextView.text = getString(R.string.note, res.note)
            res.audioLocation?.let { path -> setAudio(path) }
            soundRecordProgressView.visibility = if (res.audioLocation != null) View.VISIBLE else View.GONE
            res.guid?.let {
                reportImageAdapter.setImages(viewModel.getImagesByCoreId(it), false)
                assetsTextView.visibility =
                    if (viewModel.getImagesByCoreId(it).isEmpty() && res.note == null && viewModel.getTrackingByCoreId(
                            it
                        ) == null
                    ) View.GONE else View.VISIBLE
            }
        }
    }
    
    private fun getMessageList(answers: List<Int>): List<AnswerItem> {
        val sorted: List<Int> = answers.sortedWith(
            compareBy({ it.toString()[0] == '2' },
                { it.toString()[0] == '7' },
                { it.toString()[0] == '6' },
                { it.toString()[0] == '3' },
                { it.toString()[0] == '1' })
        )
        val answerItems = arrayListOf<AnswerItem>()
        sorted.forEach {
            it.getAnswerItem(this)?.let { item -> answerItems.add(item) }
        }
        return answerItems
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
                }
                SoundRecordState.PLAYING -> {
                    startPlaying()
                }
                SoundRecordState.STOP_PLAYING -> {
                    stopPlaying()
                    cancelButton.visibility = View.GONE
                }
                SoundRecordState.RECORDING -> {}
                SoundRecordState.STOPPED_RECORD -> {}
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
    
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapBoxMap = mapboxMap
        mapboxMap.uiSettings.apply {
            setAllGesturesEnabled(false)
            isAttributionEnabled = false
            isLogoEnabled = false
        }
        
        mapboxMap.setStyle(Style.OUTDOORS) { style ->
            setupSources(style)
            
            response?.let { res ->
                res.guid?.let { id ->
                    val track = viewModel.getTrackingByCoreId(id)
                    if (track != null) {
                        val tempTrack = arrayListOf<Feature>()
                        val json = File(track.localPath).readText()
                        val featureCollection = FeatureCollection.fromJson(json)
                        val feature = featureCollection.features()?.get(0)
                        feature?.let {
                            tempTrack.add(it)
                        }
                        addLineLayer(style)
                        lineSource?.setGeoJson(FeatureCollection.fromFeatures(tempTrack))
                        
                        val lastLocation = feature?.geometry() as LineString
                        val pointFeatures = lastLocation.coordinates().map {
                            Feature.fromGeometry(Point.fromLngLat(it.longitude(), it.latitude()))
                        }
                        checkInSource?.setGeoJson(FeatureCollection.fromFeatures(pointFeatures))
                        moveCameraToLeavesBounds(lastLocation.coordinates())
                    } else {
                        mapView.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun moveCameraToLeavesBounds(features: List<Point>) {
        val latLngList: ArrayList<LatLng> = ArrayList()
        for (singleClusterFeature in features) {
            latLngList.add(LatLng(singleClusterFeature.latitude(), singleClusterFeature.longitude()))
        }
        if (latLngList.size > 1) {
            moveCameraWithLatLngList(latLngList)
        } else {
            moveCamera(latLngList[0])
        }
    }
    
    private fun moveCameraWithLatLngList(latLngList: List<LatLng>) {
        val latLngBounds = LatLngBounds.Builder()
            .includes(latLngList)
            .build()
        mapBoxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 230), 1300)
    }
    
    private fun moveCamera(loc: LatLng) {
        mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10.0))
    }
    
    private fun setupSources(style: Style) {
        lineSource = GeoJsonSource(SOURCE_LINE)
        lineSource?.let { style.addSource(it) }
        
        checkInSource = GeoJsonSource(SOURCE_CHECK_IN)
        checkInSource?.let { style.addSource(it) }
    }
    
    private fun addLineLayer(style: Style) {
        val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(Expression.get("color"))
        )
        style.addLayer(lineLayer)
        
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_circle_tracking, null)
        val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
        mBitmap?.let { style.addImage(MARKER_CHECK_IN_IMAGE, it) }
        
        val checkInLayer = SymbolLayer(MARKER_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
            withProperties(
                PropertyFactory.iconImage(MARKER_CHECK_IN_IMAGE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconSize(1f)
            )
        }
        style.addLayer(checkInLayer)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbarLayout)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            elevation = 0f
            title = "#" + response?.incidentRef + " " + response?.streamName
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPlaying()
        mapView.onDestroy()
    }
    
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        analytics.trackScreen(Screen.RESPONSE_DETAIL)
    }
    
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
