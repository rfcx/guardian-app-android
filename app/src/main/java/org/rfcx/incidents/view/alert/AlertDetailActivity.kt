package org.rfcx.incidents.view.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_detail.*
import org.rfcx.incidents.R

class AlertDetailActivity : AppCompatActivity() {
	
	companion object {
		const val EXTRA_ALERT_ID = "EXTRA_ALERT_ID"
		fun startActivity(context: Context, alertId: String) {
			val intent = Intent(context, AlertDetailActivity::class.java)
			intent.putExtra(EXTRA_ALERT_ID, alertId)
			context.startActivity(intent)
		}
	}
	
	private var alertId: String? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_alert_detail)
		alertId = intent?.getStringExtra(EXTRA_ALERT_ID)
		setupToolbar()
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarLayout)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.event_detail)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
}
