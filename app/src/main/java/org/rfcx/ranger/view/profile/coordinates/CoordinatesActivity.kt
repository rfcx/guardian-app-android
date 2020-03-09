package org.rfcx.ranger.view.profile.coordinates

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_coordinates.*
import kotlinx.android.synthetic.main.activity_feedback.toolbar
import org.rfcx.ranger.R

class CoordinatesActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_coordinates)
		
		setupToolbar()
		
		ddLayout.setOnClickListener {
			
			checkDDImageView.visibility = View.VISIBLE
			checkDDMImageView.visibility = View.INVISIBLE
			checkDMSImageView.visibility = View.INVISIBLE
			finish()
			
		}
		
		ddmLayout.setOnClickListener {
			
			checkDDImageView.visibility = View.INVISIBLE
			checkDDMImageView.visibility = View.VISIBLE
			checkDMSImageView.visibility = View.INVISIBLE
			finish()
			
		}
		
		dmsLayout.setOnClickListener {
			
			checkDDImageView.visibility = View.INVISIBLE
			checkDDMImageView.visibility = View.INVISIBLE
			checkDMSImageView.visibility = View.VISIBLE
			finish()
			
		}
		
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.coordinates)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, CoordinatesActivity::class.java)
			context.startActivity(intent)
		}
	}
}
