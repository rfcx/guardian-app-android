package org.rfcx.ranger.view.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_feedback.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.util.*
import java.io.File

class FeedbackActivity : AppCompatActivity() {
	
	private val feedbackViewModel: FeedbackViewModel by viewModel()
	private var imageFile: File? = null
	private val galleryPermissions by lazy { GalleryPermissions(this) }
	private val feedbackImageAdapter by lazy { FeedbackImageAdapter() }
	var pathListArray: List<String>? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_feedback)
		
		setupToolbar()
		setEmail()
		setupFeedbackImages()
	}
	
	private fun setupFeedbackImages() {
		feedbackRecycler.apply {
			layoutManager = LinearLayoutManager(this@FeedbackActivity)
			adapter = feedbackImageAdapter
		}
		
		feedbackImageAdapter.onFeedbackImageAdapterClickListener = object : OnFeedbackImageAdapterClickListener {
			override fun onDeleteImageClick(position: Int) {
				feedbackImageAdapter.removeAt(position)
			}
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		handleGalleryResult(requestCode, resultCode, data)
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (BuildConfig.DEBUG) {
			val inflater = menuInflater
			inflater.inflate(R.menu.feedback_menu, menu)
		}
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
			R.id.attachView -> openGallery()
			R.id.sendEmailView -> sendEmail()
		}
		return super.onOptionsItemSelected(item)
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
		if (feedbackImageAdapter.getImageCount() < FeedbackImageAdapter.MAX_IMAGE_SIZE) {
			val remainingImage = FeedbackImageAdapter.MAX_IMAGE_SIZE - feedbackImageAdapter.getImageCount()
			Matisse.from(this)
					.choose(MimeType.ofImage())
					.countable(true)
					.maxSelectable(remainingImage)
					.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
					.thumbnailScale(0.85f)
					.imageEngine(GlideV4ImageEngine())
					.theme(R.style.Matisse_Dracula)
					.forResult(ReportUtils.REQUEST_GALLERY)
		} else {
			Toast.makeText(this, R.string.maximum_number_of_attachments, Toast.LENGTH_LONG).show()
		}
	}
	
	private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return
		
		val pathList = mutableListOf<String>()
		val results = Matisse.obtainResult(intentData)
		results.forEach {
			val imagePath = ImageFileUtils.findRealPath(this, it)
			imagePath?.let { path ->
				pathList.add(path)
			}
		}
		feedbackImageAdapter.addImages(pathList)
		pathListArray = pathList
	}
	
	private fun sendEmail() {
		val feedbackInput = feedbackEditText.text.toString()
		if(feedbackInput.isNotEmpty()) {
			feedbackViewModel.uploadFile(pathListArray, feedbackInput)
		}else{
			Toast.makeText(this, "Data Stored", Toast.LENGTH_SHORT).show()
		}
	}
	
	@SuppressLint("SetTextI18n")
	fun setEmail() {
		val preferences = Preferences.getInstance(this)
		val email = preferences.getString(Preferences.EMAIL, preferences.getString(Preferences.USER_GUID, ""))
		fromEmailTextView.text = "${getString(R.string.from)} ${email}"
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.profile_send_feedback)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, FeedbackActivity::class.java)
			context.startActivity(intent)
		}
	}
}
