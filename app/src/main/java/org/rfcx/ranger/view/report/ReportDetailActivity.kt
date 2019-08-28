package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.ReportImageAdapter
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.widget.OnStateChangeListener
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File
import java.io.IOException

class ReportDetailActivity : BaseActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	
	private var mapView: GoogleMap? = null
	private var location: LatLng? = null
	private var audioFile: File? = null
	private var player: MediaPlayer? = null
	private val reportImageAdapter by lazy { ReportImageAdapter() }
	
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
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_on_map_repost)))
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
		audioProgressView.onStateChangeListener = object : OnStateChangeListener {
			override fun onStateChanged(state: SoundRecordState) {
				when (state) {
					SoundRecordState.PLAYING -> {
						startPlaying()
					}
					SoundRecordState.STOP_PLAYING -> {
						stopPlaying()
					}
					
				}
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
	
	private fun updateReportImages() {
//		val newAttachImages = reportImageAdapter.getNewAttachImage()
//		val reportImageDb = ReportImageDb()
//
//		reportImageDb.save(report!!, newAttachImages)
//		ImageUploadWorker.enqueue()
//		finish()
	}
	
	private fun setupImageRecycler() {
		imageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(this@ReportDetailActivity, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		
//		reportImageAdapter.onReportImageAdapterClickListener = object : OnReportImageAdapterClickListener {
//			override fun onAddImageClick() {
//				attachImageDialog.show()
//			}
//
//			override fun onDeleteImageClick(position: Int) {
//				reportImageAdapter.removeAt(position)
//				dismissImagePickerOptionsDialog()
//			}
//		}
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
