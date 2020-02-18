package org.rfcx.ranger.view.profile.editprofile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_feedback.toolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.util.*
import java.io.File

class EditProfileActivity : AppCompatActivity() {
	private val galleryPermissions by lazy { GalleryPermissions(this) }
	private var newImageProfilePath: String = ""
	private var menuAll: Menu? = null
	private val editProfileViewModel: EditProfileViewModel by viewModel()
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		handleGalleryResult(requestCode, resultCode, data)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_profile)
		
		setupToolbar()
		
		val imageView = ImageView(this)
		Glide.with(this).load(this.getUserProfile()).apply(RequestOptions.circleCropTransform()).into(imageView)
		profilePhotoLinearLayout.addView(imageView)
		
		profilePhotoLinearLayout.setOnClickListener {
			openGallery()
		}
		
		changeProfilePhotoTextView.setOnClickListener {
			openGallery()
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.profile_image, menu)
		menuAll = menu
		setEnableChangeProfileView()
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		R.id.attachView
		when (item?.itemId) {
			android.R.id.home -> finish()
			R.id.changeProfilePhotoView -> sendProfileImage()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun sendProfileImage() {
		Log.d("sendProfileImage", " $newImageProfilePath")
		editProfileViewModel.updateProfilePhoto(path = newImageProfilePath)
	}
	
	private fun setEnableChangeProfileView(start: Boolean = false) {
		menuAll?.findItem(R.id.changeProfilePhotoView)?.isEnabled = start
		val itemSend = menuAll?.findItem(R.id.changeProfilePhotoView)?.icon
		val wrapDrawable = itemSend?.let { DrawableCompat.wrap(it) }
		
		if (wrapDrawable != null) {
			if (start) DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, R.color.colorPrimary)) else DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, android.R.color.darker_gray))
		}
	}
	
	private fun openGallery() {
		if (!galleryPermissions.allowed()) {
			newImageProfilePath = ""
			galleryPermissions.check { }
		} else {
			startOpenGallery()
		}
	}
	
	private fun startOpenGallery() {
		Matisse.from(this)
				.choose(MimeType.ofImage())
				.countable(true)
				.maxSelectable(1)
				.restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
				.thumbnailScale(0.85f)
				.imageEngine(GlideV4ImageEngine())
				.theme(R.style.Matisse_Dracula)
				.forResult(ReportUtils.REQUEST_GALLERY)
	}
	
	private fun handleGalleryResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
		if (requestCode != ReportUtils.REQUEST_GALLERY || resultCode != Activity.RESULT_OK || intentData == null) return
		val results = Matisse.obtainResult(intentData)
		
		results.forEach {
			val imagePath = ImageFileUtils.findRealPath(this, it)
			profilePhotoLinearLayout.removeAllViews()
			
			setEnableChangeProfileView(true)
			newImageProfilePath = imagePath.toString()
			
			val imageView = ImageView(this)
			Glide.with(this).load(Uri.fromFile(File(imagePath))).apply(RequestOptions.circleCropTransform()).into(imageView)
			profilePhotoLinearLayout.addView(imageView)
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.edit_profile)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, EditProfileActivity::class.java)
			context.startActivity(intent)
		}
	}
}
