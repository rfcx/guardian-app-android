package org.rfcx.ranger.view.report

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.buttom_sheet_attach_image_layout.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.adapter.ReportImageAdapter
import org.rfcx.ranger.adapter.report.ReportTypeAdapter
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.service.ImageUploadWorker
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.*
import org.rfcx.ranger.util.ReportUtils.REQUEST_GALLERY
import org.rfcx.ranger.util.ReportUtils.REQUEST_TAKE_PHOTO
import org.rfcx.ranger.widget.OnStatChangeListener
import org.rfcx.ranger.widget.SoundRecordState
import org.rfcx.ranger.widget.WhenView
import java.io.File
import java.io.IOException

class ReportActivity : AppCompatActivity(), OnMapReadyCallback {
	
	private var googleMap: GoogleMap? = null
	private val reportAdapter = ReportTypeAdapter()
	
	private var imageFile: File? = null
	private var recordFile: File? = null
	private var recorder: MediaRecorder? = null
	private var player: MediaPlayer? = null
	private val locationPermissions by lazy { LocationPermissions(this) }
	private val recordPermissions by lazy { RecordingPermissions(this) }
	private val cameraPermissions by lazy { CameraPermissions(this) }
	private val galleryPermissions by lazy { GalleryPermissions(this) }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private var attachImages: ArrayList<Pair<String, Bitmap>> = arrayListOf()
	
	// data
	private var report: Report? = null
	private var reportImageAttachmentsCount: Int = 0
	
	private lateinit var attachImageDialog: BottomSheetDialog
	private val reportImageAdapter by lazy { ReportImageAdapter() }
	
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				markRangerLocation(it)
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String?) {}
		override fun onProviderDisabled(p0: String?) {}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report)
		
		bindActionbar()
		initReport()
		
		addAudioButton.setOnClickListener {
			onAddAudio()
		}
		
		reportButton.setOnClickListener {
			if (report == null) {
				submitReport()
			} else {
				updateReport()
			}
		}
	}
	
	private fun updateReport() {
		// Update image attachments
		// take the new image
		if (report == null) return
		val newAttachImages = arrayListOf<String>()
		val startNewImageIndex = if (reportImageAttachmentsCount == 0) 0 else reportImageAttachmentsCount
		val reportImageDb = ReportImageDb()
		for (i in startNewImageIndex until attachImages.count()) {
			newAttachImages.add(attachImages[i].first)
		}
		reportImageDb.save(report!!, newAttachImages)
		ImageUploadWorker.enqueue()
		finish()
	}
	
	private fun initReport() {
		if (intent.hasExtra(EXTRA_REPORT_ID)) {
			val reportId = intent.getIntExtra(EXTRA_REPORT_ID, 0)
			report = ReportDb().getReport(reportId)
			addAudioButton.visibility = View.INVISIBLE
			reportButton.visibility = View.GONE
			soundRecordProgressView.visibility = View.VISIBLE
		}
		
		setupMap()
		setupReportWhatAdapter()
		setupWhenView()
		setupRecordSoundProgressView()
		setupAttachImageDialog()
		setupReportImages()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		locationManager?.removeUpdates(locationListener)
		val map = supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		stopPlaying()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		locationPermissions.handleActivityResult(requestCode, resultCode)
		handleTakePhotoResult(requestCode, resultCode)
		handleGalleryResult(requestCode, resultCode, data)
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		locationPermissions.handleRequestResult(requestCode, grantResults)
		recordPermissions.handleRequestResult(requestCode, grantResults)
		cameraPermissions.handleRequestResult(requestCode, grantResults)
		galleryPermissions.handleRequestResult(requestCode, grantResults)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onMapReady(map: GoogleMap?) {
		googleMap = map
		googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
		
		if (isOnDetailView()) {
			report?.let {
				val location = Location(LocationManager.GPS_PROVIDER)
				location.latitude = it.latitude
				location.longitude = it.longitude
				markRangerLocation(location)
			}
			return
		}
		locationPermissions.check { isAllowed: Boolean ->
			if (isAllowed) {
				getLocation()
			}
		}
	}
	
	private fun bindActionbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.report_title)
		}
	}
	
	private fun setupMap() {
		val map = supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.getMapAsync(this@ReportActivity)
	}
	
	@SuppressLint("MissingPermission")
	private fun getLocation() {
		if (isDestroyed) return
		locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0f, locationListener)
//			locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000L, 0f, locationListener)
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			lastLocation?.let { markRangerLocation(it) }
		} catch (ex: java.lang.SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		}
		
	}
	
	private fun markRangerLocation(location: Location) {
		lastLocation = location
		googleMap?.clear()
		val latLng = LatLng(location.latitude, location.longitude)
		googleMap?.addMarker(MarkerOptions()
				.position(latLng)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
		
		googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
				latLng, 15f))
		googleMap?.uiSettings?.isScrollGesturesEnabled = false
		validateForm()
	}
	
	private fun setupReportWhatAdapter() {
		val layoutManager = GridLayoutManager(this@ReportActivity, 5)
		reportTypeRecycler.layoutManager = layoutManager
		reportTypeRecycler.adapter = reportAdapter
		reportAdapter.onMessageItemClickListener = object : OnMessageItemClickListener {
			override fun onMessageItemClick(position: Int) {
				validateForm()
			}
		}
		
		if (isOnDetailView()) {
			report?.let {
				reportAdapter.selectedItem = it.value.toEventPosition()
				// Disable touch event
				reportAdapter.disable()
			}
		}
	}
	
	private fun setupWhenView() {
		whenView.onWhenViewStatChangedListener = object : WhenView.OnWhenViewStatChangedListener {
			override fun onStateChange(state: WhenView.State) {
				validateForm()
			}
		}
		if (isOnDetailView()) {
			whenView.setState(WhenView.State.fromInt(report?.ageEstimate ?: 0) ?: WhenView.State.NOW)
			whenView.disable()
		}
	}
	
	private fun setupRecordSoundProgressView() {
		soundRecordProgressView.onStatChangeListener = object : OnStatChangeListener {
			override fun onStateChanged(state: SoundRecordState) {
				when (state) {
					SoundRecordState.NONE -> {
						recordFile?.deleteOnExit()
						recordFile = null
					}
					SoundRecordState.RECORDING -> {
						record()
					}
					SoundRecordState.STOPPED_RECORD -> {
						stopRecording()
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
		if (isOnDetailView()) {
			soundRecordProgressView.disableEdit()
			report?.audioLocation?.let {
				recordFile = File(it)
			}
			if (recordFile != null && recordFile!!.exists()) {
				soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
				soundRecordProgressView.visibility = View.VISIBLE
				reportRecordTextView.visibility = View.VISIBLE
			}else{
				soundRecordProgressView.visibility = View.GONE
				reportRecordTextView.visibility = View.GONE
			}
		}
	}
	
	private fun validateForm() {
		val reportTypeItem = reportAdapter.getSelectedItem()
		val whenState = whenView.getState()
		reportButton.isEnabled = reportTypeItem != null && whenState != WhenView.State.NONE && lastLocation != null
	}
	
	private fun submitReport() {
		val reportTypeItem = reportAdapter.getSelectedItem()
		val whenState = whenView.getState()
		if (reportTypeItem == null || whenState == WhenView.State.NONE) {
			validateForm()
			return
		}
		
		if (lastLocation == null) {
			if (!locationPermissions.allowed()) {
				locationPermissions.check { getLocation() }
			} else {
				Snackbar.make(rootView, R.string.report_location_null, Snackbar.LENGTH_LONG).show()
			}
			return
		}
		
		val site = getSiteName()
		val time = DateHelper.getIsoTime()
		val lat = lastLocation?.latitude ?: 0.0
		val lon = lastLocation?.longitude ?: 0.0
		Log.d("getSiteName", getSiteName())
		val report = Report(value = reportTypeItem.type, site = site, reportedAt = time,
				latitude = lat, longitude = lon, ageEstimate = whenState.ageEstimate,
				audioLocation = recordFile?.canonicalPath)
		
		val attachImageList = attachImages.map { it.first }
		ReportDb().save(report, attachImageList)
		ReportSyncWorker.enqueue()
		
		finish()
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
		recordFile = File.createTempFile("Record${System.currentTimeMillis()}", ".mp3", this.cacheDir)
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
					Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				}
				start()
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
				Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
			}
		}
		recorder = null
	}
	
	private fun startPlaying() {
		if (recordFile == null) {
			soundRecordProgressView.state = SoundRecordState.NONE
			if (isOnDetailView()) {
				soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
			}
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
				Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				e.printStackTrace()
			}
		}
	}
	
	private fun stopPlaying() {
		player?.release()
		player = null
	}
	
	private fun setupReportImages() {
		attachImageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(this@ReportActivity, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		
		report?.let {
			val reportImages = ReportDb().getReportImages(it.id)
			reportImageAttachmentsCount = reportImages?.count() ?: 0
			reportImages?.forEach { reportImage ->
				if (reportImage.imageUrl != null) {
					val image = BitmapFactory.decodeFile(reportImage.imageUrl)
					attachImages.add(Pair(reportImage.imageUrl ?: "", image))
				}
			}
			showImages()
		}
	}
	
	private fun onAddAudio() {
		addAudioButton.visibility = View.INVISIBLE
		soundRecordProgressView.visibility = View.VISIBLE
	}
	
	private fun setupAttachImageDialog() {
		val bottomSheetView = layoutInflater.inflate(R.layout.buttom_sheet_attach_image_layout, null)
		
		bottomSheetView.menuGallery.setOnClickListener {
			openGallery()
		}
		
		bottomSheetView.menuTakePhoto.setOnClickListener {
			takePhoto()
		}
		
		attachImageDialog = BottomSheetDialog(this@ReportActivity)
		attachImageDialog.setContentView(bottomSheetView)
		
		attachImageButton.setOnClickListener {
			attachImageDialog.show()
		}
	}
	
	private fun showImages() {
		if (attachImages.isEmpty()) {
			attachImageRecycler.visibility = View.GONE
			return
		}
		
		reportImageAdapter.images = attachImages.map { it.second } // send images to adapter
		attachImageRecycler.visibility = View.VISIBLE
		attachImageDialog.dismiss()
		
		if (report != null && reportImageAttachmentsCount != attachImages.count()) {
			// if have new image added
			reportButton.visibility = View.VISIBLE
		}
	}
	
	private fun takePhoto() {
		if (!cameraPermissions.allowed()) {
			imageFile = null
			cameraPermissions.check { }
		} else {
			startTakePhoto()
		}
	}
	
	private fun startTakePhoto() {
		val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		imageFile = ReportUtils.createReportImageFile()
		if (imageFile != null) {
			val photoURI = FileProvider.getUriForFile(this, ReportUtils.FILE_CONTENT_PROVIDER, imageFile!!)
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
			startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
		} else {
			// TODO: handle on can't create image file
		}
	}
	
	private fun handleTakePhotoResult(requestCode: Int, resultCode: Int) {
		if (requestCode != REQUEST_TAKE_PHOTO) return
		
		if (resultCode == Activity.RESULT_OK) {
			imageFile?.let {
				val bitmap = ImageFileUtils.resizeImage(it)
				bitmap?.let { image -> attachImages.add(Pair(it.absolutePath, image)) }
				showImages()
			}
			Log.d("photoSet", "photo size: ${attachImages.size}")
		} else {
			// remove file image
			imageFile?.let {
				ImageFileUtils.removeFile(it)
				this@ReportActivity.imageFile = null
			}
		}
	}
	
	private fun openGallery() {
		if (!galleryPermissions.allowed()) {
			imageFile = null
			galleryPermissions.check { }
		} else {
			startOpenGallery()
		}
	}
	
	private fun startOpenGallery() {
		val galleyIntent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		galleyIntent.type = "image/*"
		galleyIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
		startActivityForResult(galleyIntent, REQUEST_GALLERY)
	}
	
	private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		if (requestCode != REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return
		
		val pathList = mutableListOf<String>()
		if (intentData.data != null) {
			val imageUri = intentData.data!!
			val path = ImageFileUtils.findRealPath(this@ReportActivity, imageUri)
			path?.let { pathList.add(it) }
		} else {
			val clipData = intentData.clipData ?: return
			for (index in 0 until clipData.itemCount) {
				val item = clipData.getItemAt(index)
				val path = ImageFileUtils.findRealPath(this@ReportActivity, item.uri)
				path?.let { pathList.add(it) }
			}
		}
		
		pathList.forEach { path ->
			val image = ImageFileUtils.resizeImage(File(path))
			image?.let { attachImages.add(Pair(path, it)) }
		}
		showImages()
	}
	
	companion object {
		private const val EXTRA_REPORT_ID = "extra_report_id"
		
		fun startIntent(context: Context, reportId: Int) {
			val intent = Intent(context, ReportActivity::class.java)
			intent.putExtra(EXTRA_REPORT_ID, reportId)
			context.startActivity(intent)
		}
	}
	
	/**
	 * return Boolean of showing exist report
	 */
	private fun isOnDetailView(): Boolean = report != null
}