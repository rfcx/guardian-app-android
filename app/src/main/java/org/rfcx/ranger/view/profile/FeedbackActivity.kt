package org.rfcx.ranger.view.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_feedback.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseListItem
import org.rfcx.ranger.util.GalleryPermissions
import org.rfcx.ranger.util.GlideV4ImageEngine
import org.rfcx.ranger.util.ImageFileUtils
import org.rfcx.ranger.util.ReportUtils
import java.io.File

class FeedbackActivity : AppCompatActivity() {
	private val feedbackViewModel: FeedbackViewModel by viewModel()
	private var imageFile: File? = null
	private val galleryPermissions by lazy { GalleryPermissions(this) }
	private val feedbackImageAdapter by lazy { FeedbackImageAdapter() }
	private var pathListArray: List<String>? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_feedback)
		
		setupToolbar()
		setTextFrom()
		setupFeedbackImages()
	}
	
	private fun setupFeedbackImages() {
		feedbackRecycler.apply {
			layoutManager = LinearLayoutManager(this@FeedbackActivity)
			adapter = feedbackImageAdapter
		}
		
		feedbackImageAdapter.onFeedbackImageAdapterClickListener = object : OnFeedbackImageAdapterClickListener {
			override fun pathListArray(path: ArrayList<BaseListItem>) {
				val pathList = mutableListOf<String>()
				path.forEach {
					val itemImage = it as LocalImageItem
					pathList.add(itemImage.localPath)
				}
				pathListArray = pathList
			}
			
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
			R.id.sendFeedbackView -> sendFeedback()
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
	}
	
	private fun sendFeedback() {
		val itemView = findViewById<View>(R.id.sendFeedbackView)
		itemView.hideKeyboard()
		val feedbackInput = feedbackEditText.text.toString()
		if (feedbackInput.isNotEmpty()) {
			feedbackViewModel.saveDataInFirestore(pathListArray, feedbackInput)
		} else {
			Toast.makeText(this, getString(R.string.please_enter_feedback), Toast.LENGTH_SHORT).show()
		}
	}
	
	private fun View.hideKeyboard() = this.let {
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(windowToken, 0)
	}
	
	@SuppressLint("SetTextI18n")
	fun setTextFrom() {
		val fromWho = feedbackViewModel.from()
		fromEmailTextView.text = "${getString(R.string.from)} $fromWho"
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

