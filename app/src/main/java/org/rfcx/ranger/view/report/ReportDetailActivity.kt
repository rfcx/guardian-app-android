package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.widget.SoundRecordState
import java.io.File

class ReportDetailActivity : BaseActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	
	private var mapView: GoogleMap? = null
	private var location: LatLng? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report_detail)
		setupToolbar()
		
		audioProgressView.disableEdit()
		
		val reportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
		viewModel.setReport(reportId)
		
		viewModel.getReport().observe(this, Observer { report ->
			if (report == null) {
				reportTypeTextView.text = getString(R.string.report_type_other)
				reportTypeImageView.setImageResource(R.drawable.ic_other)
			} else {
				reportTypeTextView.text = report.getLocalisedValue(this)
				reportTypeImageView.setImageResource(report.getImageResource())
				dateTimeTextView.text = report.getReportedAtRelative(this)
				whenTextView.text = report.getLocalisedAgeEstimate(this)

				this.location = LatLng(report.latitude, report.longitude)
				setupMapPin()

				setupAudioPlayer(report.audioLocation)
			}
		})
		
//		viewModel.getReportImages().observe(this, Observer {
//			if (reportImages != null) {
//				reportImageAdapter.setImages(reportImages)
//			}
//		})
		
		val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		mapFragment?.getMapAsync {
			mapView = it
			mapView?.mapType = GoogleMap.MAP_TYPE_SATELLITE
			mapView?.uiSettings?.isScrollGesturesEnabled = false
			runOnUiThread { setupMapPin() }
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
	
	private fun setupMapPin() {
		val mapView = mapView
		val location = location
		if (mapView == null || location == null) {
			return
		}
		mapView.clear()
		mapView.addMarker(MarkerOptions().position(location)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_report_pin_on_map)))
		mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(
				location, 15f))
	}
	
	private fun setupAudioPlayer(audioLocation: String?) {
		var file: File? = audioLocation?.let { File(it) }
		if (file != null && file.exists()) {
			audioProgressView.state = SoundRecordState.STOP_PLAYING
			audioProgressView.visibility = View.VISIBLE
			audioNoneLabel.visibility = View.GONE
		} else {
			audioProgressView.visibility = View.GONE
			audioNoneLabel.visibility = View.VISIBLE
		}
	}
	
	private fun updateReportImages() {
//		val newAttachImages = reportImageAdapter.getNewAttachImage()
//		val reportImageDb = ReportImageDb()
//
//		reportImageDb.save(report!!, newAttachImages)
//		ImageUploadWorker.enqueue()
//		finish()
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
