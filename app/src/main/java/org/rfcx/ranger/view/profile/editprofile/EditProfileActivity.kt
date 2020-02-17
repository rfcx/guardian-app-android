package org.rfcx.ranger.view.profile.editprofile

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_feedback.toolbar
import org.rfcx.ranger.R
import org.rfcx.ranger.util.getUserProfile

class EditProfileActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_edit_profile)
		
		setupToolbar()
		
		val imageView = ImageView(this)
		Glide.with(this).load(this.getUserProfile()).apply(RequestOptions.circleCropTransform()).into(imageView)
		profilePhotoLinearLayout.addView(imageView)
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
