package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.ActivityReportDetailBinding
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.*
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.util.*

class ReportDetailActivity : BaseReportImageActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	private lateinit var mapView: MapView
	private lateinit var mapBoxMap: MapboxMap
	private lateinit var location: LatLng
	private var audioFile: File? = null
	private var player: MediaPlayer? = null
	private val analytics by lazy { Analytics(this) }
	private var urlBeforeGetShortLink: String? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Mapbox.getInstance(this, getString(R.string.mapbox_token))
		val binding: ActivityReportDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_report_detail)
		binding.context = this
		setupToolbar()
		setupAudioPlaying()
		setupImageRecycler()
		
		mapView = findViewById(R.id.mapBoxView)
		mapView.onCreate(savedInstanceState)
		
		val reportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
		viewModel.setReport(reportId)
		
		viewModel.getReport().observe(this, Observer { report ->
			getShortLink(report)
			
			if (report == null) {
				reportTypeTextView.text = getString(R.string.other)
				reportTypeImageView.setImageResource(R.drawable.ic_pin_huge)
			} else {
				binding.report = DetailReport(report, this)
				this.location = LatLng(report.latitude, report.longitude)
				setAudio(report.audioLocation, binding)
			}
		})
		
		viewModel.getReportImages().observe(this, Observer { images ->
			reportImageAdapter.setImages(images)
		})
		
		mapView.getMapAsync { mapboxMap ->
			mapBoxMap = mapboxMap
			mapboxMap.uiSettings.apply {
				setAllGesturesEnabled(false)
				isAttributionEnabled = false
				isLogoEnabled = false
			}
			
			mapboxMap.setStyle(Style.OUTDOORS) {
				setMapPin()
			}
		}
		
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.share_reports, menu)
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		R.id.attachView
		when (item?.itemId) {
			android.R.id.home -> finish()
			R.id.shareReportsView -> shareShortLinkReports()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun getShortLink(report: Report) {
		val date = Date(Timestamp((report.reportedAt.time - TimeZone.getDefault().rawOffset) + TimeZone.getTimeZone("America/Belem").rawOffset).time)
		
		val timeStart = Date((date.time.minus((10000)).let { Timestamp(it) }).time)
		val timeEnd = Date((date.time.plus((10000)).let { Timestamp(it) }).time)
		
		val site = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE)
		val url = "https://dashboard.rfcx.org/rangers?site=$site&live-view=false&rangers-tab=reports&wds=%5B%220%22,%221%22,%222%22,%223%22,%224%22,%225%22,%226%22%5D&start-aft=${timeStart.dateForShortLink()}&end-bef=${timeEnd.dateForShortLink()}&range=Custom%20Range&dayt-start-aft=00:00:00&dayt-end-bef=00:00:00"
		urlBeforeGetShortLink = url
		viewModel.getShortLink(url)
	}
	
	private fun shareShortLinkReports() {
		viewModel.shortLink.observe(this, Observer {
			shareReports(it)
		})
	}
	
	private fun shareReports(shortLink: String) {
		val s: String = if (shortLink == "") {
			urlBeforeGetShortLink.toString()
		} else {
			"$shortLink (${this.getString(R.string.expires_in_24h)})"
		}
		
		//Intent to share the text
		val shareIntent = Intent()
		shareIntent.action = Intent.ACTION_SEND
		shareIntent.type = "text/plain"
		shareIntent.putExtra(Intent.EXTRA_TEXT, s)
		startActivity(Intent.createChooser(shareIntent, "Share via"))
	}
	
	data class DetailReport(val report: Report, val context: Context) {
		fun getImage(): Int = report.getImageResource()
		fun getReportValue(): String = report.getLocalisedValue(context)
		fun getDateTime(): String = report.getReportedAtRelative(context)
		fun getWhenText(): String = report.getLocalisedAgeEstimate(context)
		fun getNote(): String {
			return if (report.notes.isNullOrEmpty()) {
				""
			} else {
				report.notes.toString()
			}
		}
	}
	
	override fun onStart() {
		super.onStart()
		mapView.onStart()
	}
	
	override fun onResume() {
		super.onResume()
		mapView.onResume()
		analytics.trackScreen(Screen.REPORTDETAIL)
	}
	
	override fun onPause() {
		super.onPause()
		mapView.onPause()
	}
	
	override fun onStop() {
		super.onStop()
		mapView.onStop()
	}
	
	override fun onLowMemory() {
		super.onLowMemory()
		mapView.onLowMemory()
	}
	
	override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
		super.onSaveInstanceState(outState, outPersistentState)
		mapView.onSaveInstanceState(outState)
		
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.report_detail_title)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	private fun setMapPin() {
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0))
		
		val symbolManager = mapBoxMap.style?.let { SymbolManager(mapView, mapBoxMap, it) }
		symbolManager?.iconAllowOverlap = true
		symbolManager?.iconIgnorePlacement = true
		
		val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
		val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
		if (mBitmap != null) {
			mapBoxMap.style?.addImage("pin-map", mBitmap)
		}
		
		symbolManager?.create(SymbolOptions()
				.withLatLng(LatLng(location.latitude, location.longitude))
				.withIconImage("pin-map")
				.withIconSize(1.0f))
	}
	
	private fun setAudio(path: String?, binding: ActivityReportDetailBinding) {
		audioFile = path?.let { File(it) }
		binding.audio = Audio(audioFile)
		
		if (audioFile?.exists() == true) {
			audioProgressView.state = SoundRecordState.STOP_PLAYING
		}
	}
	
	data class Audio(val audioFile: File?) {
		fun getVisibility(): Int {
			return if (audioFile?.exists() == true) {
				View.VISIBLE
			} else {
				View.GONE
			}
		}
		
		fun getNoneLabelVisibility(): Int {
			return if (audioFile?.exists() == true) {
				View.GONE
			} else {
				View.VISIBLE
			}
		}
	}
	
	private fun setupAudioPlaying() {
		audioProgressView.disableEdit()
		audioProgressView.onStateChangeListener = { state ->
			when (state) {
				SoundRecordState.PLAYING -> startPlaying()
				SoundRecordState.STOP_PLAYING -> stopPlaying()
				else -> {
				}
			}
		}
	}
	
	override fun onDestroy() {
		saveEditedNoteIfChange()
		super.onDestroy()
		stopPlaying()
	}
	
	private fun startPlaying() {
		val file = audioFile
		if (file == null) {
			audioProgressView.state = SoundRecordState.NONE
			return
		}
		player = MediaPlayer().apply {
			try {
				setDataSource(file.absolutePath)
				prepare()
				start()
				setOnCompletionListener {
					audioProgressView.state = SoundRecordState.STOP_PLAYING
				}
			} catch (e: IOException) {
				audioProgressView.state = SoundRecordState.STOP_PLAYING
				Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				e.printStackTrace()
			}
		}
	}
	
	private fun stopPlaying() {
		player?.release()
		player = null
	}
	
	override fun didAddImages(imagePaths: List<String>) {
		viewModel.addReportImages(imagePaths)
	}
	
	override fun didRemoveImage(imagePath: String) {
		viewModel.removeReportImage(imagePath)
	}
	
	private fun setupImageRecycler() {
		imageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(this@ReportDetailActivity, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
	}
	
	private fun saveEditedNoteIfChange() {
		val note: String? = if (noteTextView.text?.trim().toString().isEmpty()) {
			""
		} else noteTextView.text?.trim().toString()
		viewModel.saveEditedNoteIfChanged(note)
	}
	
	companion object {
		private const val EXTRA_REPORT_ID = "extra_report_id"
		
		fun startIntent(context: Context?, reportId: Int) {
			context?.let {
				val intent = Intent(it, ReportDetailActivity::class.java)
				intent.putExtra(EXTRA_REPORT_ID, reportId)
				it.startActivity(intent)
			}
		}
	}
}

