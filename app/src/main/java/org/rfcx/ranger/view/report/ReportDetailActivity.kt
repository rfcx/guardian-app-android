package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.ActivityReportDetailBinding
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.util.toIsoString
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.util.*

class ReportDetailActivity : BaseReportImageActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	
	private var mapView: GoogleMap? = null
	private var location: LatLng? = null
	private var audioFile: File? = null
	private var player: MediaPlayer? = null
	private val analytics by lazy { Analytics(this) }
	private var lastReport: Report? = null
	
	private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
		return ContextCompat.getDrawable(context, vectorResId)?.run {
			setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
			draw(Canvas(bitmap))
			BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val binding: ActivityReportDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_report_detail)
		binding.context = this
		setupToolbar()
		setupAudioPlaying()
		setupImageRecycler()
		
		val reportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
		viewModel.setReport(reportId)
		
		viewModel.getReport().observe(this, Observer { report ->
			lastReport = report
			if (report == null) {
				reportTypeTextView.text = getString(R.string.other)
				reportTypeImageView.setImageResource(R.drawable.ic_pin_huge)
			} else {
				binding.report = DetailReport(report, this)
				this.location = LatLng(report.latitude, report.longitude)
				setMapPin()
				
				setAudio(report.audioLocation, binding)
			}
		})
		
		viewModel.getReportImages().observe(this, Observer { images ->
			reportImageAdapter.setImages(images)
		})
		
		val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		mapFragment?.getMapAsync {
			mapView = it
			mapView?.mapType = GoogleMap.MAP_TYPE_SATELLITE
			mapView?.uiSettings?.isScrollGesturesEnabled = false
			val horizontalPadding = 16.px
			mapView?.setPadding(horizontalPadding, 0, horizontalPadding, 0)
			runOnUiThread { setMapPin() }
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
			R.id.shareReportsView -> getShortLink()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun getShortLink() {
		val timeStart = (lastReport?.reportedAt?.time?.minus((28802 * 1000L))?.let { Timestamp(it) })?.time?.let { Date(it) }
		val timeEnd = (lastReport?.reportedAt?.time?.minus((28798 * 1000L))?.let { Timestamp(it) })?.time?.let { Date(it) }
		Log.d("URL1", " ${lastReport?.reportedAt}")
		Log.d("URL1", " ${lastReport?.reportedAt?.toIsoString()}")
//		val timeStart = dateParser(lastReport?.reportedAt?.toFullDateTimeString())
//		val timeEnd = timeStart?.time?.plus((59* 1000L))?.let { Timestamp(it) }
//		Log.d("URL1","${timeStart?.toIsoString()}")
//		Log.d("URL1","${timeEnd?.toIsoString()}")
//
		val site = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE)
		
		val url = "https://dashboard.rfcx.org/rangers?site=$site&live-view=false&rangers-tab=reports&rngs=%5B%22d98489c7-ff04-4cab-8f1d-7af76deec298%22%5D&wds=%5B%220%22,%221%22,%222%22,%223%22,%224%22,%225%22,%226%22%5D&start-aft=${timeStart?.toIsoString()}&end-bef=${timeEnd?.toIsoString()}&range=Custom%20Range&dayt-start-aft=00:00:00&dayt-end-bef=00:00:00"
//		val url = "https://dashboard.rfcx.org/rangers?=$site&wds=%5B%220%22,%221%22,%222%22,%223%22,%224%22,%225%22,%226%22%5D&start-aft=${timeStart?.toIsoString()}&end-bef=${timeEnd?.toIsoString()}&range=Custom%20Range&live-view=false&rangers-tab=reports"
//				"https://dashboard.rfcx.org/rangers?site=$site&wds=%5B%220%22,%221%22,%222%22,%223%22,%224%22,%225%22,%226%22%5D&start-aft=$timeStart&end-bef=$timeEnd&range=Custom%20Range&live-view=false&rangers-tab=reports"
		Log.d("URL1", " $url")
		viewModel.getShortLink(url)
		viewModel.shortLink.observe(this, Observer {
			shareReports(it)
		})
	}
	
	fun shareReports(shortLink: String) {
		val s = "$shortLink \nLink is copied to clipboard (expires in 24h)"
		
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
	
	override fun onResume() {
		super.onResume()
		analytics.trackScreen(Screen.REPORTDETAIL)
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
		val mapView = mapView
		val location = location
		if (mapView == null || location == null) {
			return
		}
		mapView.clear()
		mapView.addMarker(MarkerOptions().position(location)
				.icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin_map)))
		mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
				location, 15f))
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

val Int.px: Int
	get() = (this * Resources.getSystem().displayMetrics.density).toInt()
