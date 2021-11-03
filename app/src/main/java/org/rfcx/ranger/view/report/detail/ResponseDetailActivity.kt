package org.rfcx.ranger.view.report.detail

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_response_detail.*
import kotlinx.android.synthetic.main.activity_response_detail.attachImageRecycler
import kotlinx.android.synthetic.main.activity_response_detail.noteTextView
import kotlinx.android.synthetic.main.activity_response_detail.soundRecordProgressView
import kotlinx.android.synthetic.main.fragment_assets.*
import kotlinx.android.synthetic.main.widget_sound_record_progress.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.*
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.ranger.view.report.create.image.ReportImageAdapter
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException


class ResponseDetailActivity : AppCompatActivity(), OnMapReadyCallback {
	
	companion object {
		const val EXTRA_RESPONSE_CORE_ID = "EXTRA_RESPONSE_CORE_ID"
		private const val SOURCE_LINE = "source.line"
		
		fun startActivity(context: Context, responseCoreId: String) {
			val intent = Intent(context, ResponseDetailActivity::class.java)
			intent.putExtra(EXTRA_RESPONSE_CORE_ID, responseCoreId)
			context.startActivity(intent)
		}
	}
	
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
				assetsTextView.visibility = if (viewModel.getImagesByCoreId(it).isEmpty() && res.note == null && viewModel.getTrackingByCoreId(it) == null) View.GONE else View.VISIBLE
			}
		}
	}
	
	private fun getMessageList(answers: List<Int>): List<AnswerItem> {
		val answerList = arrayListOf<AnswerItem>()
		answers.forEach { id ->
			id.getAnswerItem()?.let { item -> answerList.add(item) }
		}
		return answerList
	}
	
	private fun Int.getAnswerItem(): AnswerItem? {
		return when {
			// LoggingScale
			this == LoggingScale.NONE.value -> {
				AnswerItem(getString(R.string.logging_scale) + " " + getString(R.string.common_none), R.color.logging_color)
			}
			this == LoggingScale.LARGE.value -> {
				AnswerItem(getString(R.string.logging_scale) + " " + getString(R.string.large_text), R.color.logging_color)
			}
			this == LoggingScale.SMALL.value -> {
				AnswerItem(getString(R.string.logging_scale) + " " + getString(R.string.small_text), R.color.logging_color)
			}
			
			// DamageScale
			this == DamageScale.NO_VISIBLE.value -> {
				AnswerItem(getString(R.string.damage) + " " + getString(R.string.no_visible), R.color.damage_color)
			}
			this == DamageScale.SMALL.value -> {
				AnswerItem(getString(R.string.damage) + " " + getString(R.string.small_trees_cut_down), R.color.damage_color)
			}
			this == DamageScale.MEDIUM.value -> {
				AnswerItem(getString(R.string.damage) + " " + getString(R.string.medium_trees_cut_down), R.color.damage_color)
			}
			this == DamageScale.LARGE.value -> {
				AnswerItem(getString(R.string.damage) + " " + getString(R.string.large_area_clear_cut), R.color.damage_color)
			}
			
			// EvidenceTypes
			this == EvidenceTypes.NONE.value -> {
				AnswerItem(getString(R.string.common_none), R.color.evidence_color)
			}
			this == EvidenceTypes.CUT_DOWN_TREES.value -> {
				AnswerItem(getString(R.string.cut_down_trees), R.color.evidence_color)
			}
			this == EvidenceTypes.CLEARED_AREAS.value -> {
				AnswerItem(getString(R.string.cleared_areas), R.color.evidence_color)
			}
			this == EvidenceTypes.LOGGING_EQUIPMENT.value -> {
				AnswerItem(getString(R.string.logging_equipment), R.color.evidence_color)
			}
			this == EvidenceTypes.LOGGERS_AT_SITE.value -> {
				AnswerItem(getString(R.string.loggers_at_site), R.color.evidence_color)
			}
			this == EvidenceTypes.ILLEGAL_CAMPS.value -> {
				AnswerItem(getString(R.string.illegal_camps), R.color.evidence_color)
			}
			this == EvidenceTypes.FIRED_BURNED_AREAS.value -> {
				AnswerItem(getString(R.string.fires_burned_areas), R.color.evidence_color)
			}
			this == EvidenceTypes.EVIDENCE_OF_POACHING.value -> {
				AnswerItem(getString(R.string.evidence_of_poaching), R.color.evidence_color)
			}
			
			// Actions
			this == Actions.NONE.value -> {
				AnswerItem(getString(R.string.common_none), R.color.action_color)
			}
			this == Actions.COLLECTED_EVIDENCE.value -> {
				AnswerItem(getString(R.string.collected_evidence), R.color.action_color)
			}
			this == Actions.ISSUE_A_WARNING.value -> {
				AnswerItem(getString(R.string.issue_a_warning), R.color.action_color)
			}
			this == Actions.CONFISCATED_EQUIPMENT.value -> {
				AnswerItem(getString(R.string.confiscated_equipment), R.color.action_color)
			}
			this == Actions.ARRESTS.value -> {
				AnswerItem(getString(R.string.arrests), R.color.action_color)
			}
			this == Actions.PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT.value -> {
				AnswerItem(getString(R.string.planning_security), R.color.action_color)
			}
			this == Actions.OTHER.value -> {
				AnswerItem(getString(R.string.other_text), R.color.action_color)
			}
			else -> null
		}
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
		style.addSource(lineSource!!)
	}
	
	private fun addLineLayer(style: Style) {
		val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
				PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
				PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
				PropertyFactory.lineWidth(5f),
				PropertyFactory.lineColor(Expression.get("color"))
		)
		style.addLayer(lineLayer)
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
