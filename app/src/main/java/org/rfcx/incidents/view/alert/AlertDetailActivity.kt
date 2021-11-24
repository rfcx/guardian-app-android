package org.rfcx.incidents.view.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.events.detail.GuardianEventDetailViewModel

class AlertDetailActivity : AppCompatActivity() {
	private val viewModel: AlertDetailViewModel by viewModel()
	
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
		
		val alert = alertId?.let { viewModel.getAlert(it) }
		guardianNameTextView.text = alert?.classification?.title
		timeTextView.text = alert?.createdAt?.toTimeSinceStringAlternativeTimeAgo(this)
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarLayout)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.guardian_event_detail)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
}
