package org.rfcx.ranger.view.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_feedback.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.view.DiagnosticsLocationActivity

class FeedbackActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_feedback)
		
		setupToolbar()
		setEmail()
		
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
			R.id.attachView -> attachAction()
			R.id.sendEmailView -> sendEmail()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun attachAction() {
		Log.d("attachAction","attachAction")
	}
	
	private fun sendEmail() {
		Log.d("sendEmail","sendEmail")
	}
	
	@SuppressLint("SetTextI18n")
	fun setEmail(){
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
