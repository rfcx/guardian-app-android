package org.rfcx.incidents.view.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.setReportImage
import org.rfcx.incidents.util.toIsoFormatString
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo

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
		val token = this.getTokenID()
		
		alert?.let {
			spectrogramImageView.setReportImage(
					url = setFormatUrlOfSpectrogram(it),
					fromServer = true,
					token = token,
					progressBar = loadingImageProgressBar
			)
		}
	}
	
	private fun setFormatUrlOfSpectrogram(alert: Alert): String {
		return "${BuildConfig.RANGER_API_DOMAIN}/media/${alert.streamId}_t${alert.start.toIsoFormatString()}.${alert.end.toIsoFormatString()}_rfull_g1_fspec_d600.512_wdolph_z120.png"
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
