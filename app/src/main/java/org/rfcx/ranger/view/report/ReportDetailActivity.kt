package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException

class ReportDetailActivity : BaseReportImageActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	
	private var mapView: GoogleMap? = null
	private var location: LatLng? = null
	private var audioFile: File? = null
	private var player: MediaPlayer? = null
	private val analytics by lazy { Analytics(this) }
	
	
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
		setContentView(R.layout.activity_report_detail)
		setupToolbar()
		setupAudioPlaying()
		setupImageRecycler()
		
		val reportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
		viewModel.setReport(reportId)
		
		viewModel.getReport().observe(this, Observer { report ->
			if (report == null) {
				reportTypeTextView.text = getString(R.string.other)
				reportTypeImageView.setImageResource(R.drawable.ic_pin_huge)
			} else {
				reportTypeTextView.text = report.getLocalisedValue(this)
				reportTypeImageView.setImageResource(report.getImageResource())
				dateTimeTextView.text = report.getReportedAtRelative(this)
				whenTextView.text = report.getLocalisedAgeEstimate(this)

				this.location = LatLng(report.latitude, report.longitude)
				setMapPin()
				
				setAudio(report.audioLocation)
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
			mapView?.setPadding(horizontalPadding,0,horizontalPadding,0)
			runOnUiThread { setMapPin() }
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
	
	private fun setAudio(path: String?) {
		audioFile = path?.let { File(it) }
		if (audioFile?.exists() == true) {
			audioProgressView.state = SoundRecordState.STOP_PLAYING
			audioProgressView.visibility = View.VISIBLE
			audioNoneLabel.visibility = View.GONE
		} else {
			audioProgressView.visibility = View.GONE
			audioNoneLabel.visibility = View.VISIBLE
		}
	}
	
	private fun setupAudioPlaying() {
		audioProgressView.disableEdit()
		audioProgressView.onStateChangeListener = { state ->
			when (state) {
				SoundRecordState.PLAYING -> startPlaying()
				SoundRecordState.STOP_PLAYING -> stopPlaying()
				else -> {}
			}
		}
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
