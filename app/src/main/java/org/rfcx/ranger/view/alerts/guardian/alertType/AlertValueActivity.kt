package org.rfcx.ranger.view.alerts.guardian.alertType

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_by_value.*
import org.rfcx.ranger.R

class AlertValueActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_alert_by_value)
		setupToolbar()
		setIntent()
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarForType)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			if (intent.hasExtra(EXTRA_GUARDIAN_NAME)) {
				title = intent.getStringExtra(EXTRA_GUARDIAN_NAME)
				subtitle = intent.getStringExtra(EXTRA_ALERT_LABEL)
			}
		}
	}
	
	private fun setIntent() {
		if (intent.hasExtra(EXTRA_ALERT_VALUE) && intent.hasExtra(EXTRA_ALERT_VALUE)) {
			val value = intent.getStringExtra(EXTRA_ALERT_VALUE)
			val guardianName = intent.getStringExtra(EXTRA_GUARDIAN_NAME)
			if (guardianName != null) {
				supportFragmentManager.beginTransaction()
						.replace(alertDetailByTypeContainer.id,
								AlertValueFragment.newInstance(value, guardianName)).commit()
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		const val EXTRA_ALERT_VALUE = "EXTRA_ALERT_VALUE"
		const val EXTRA_ALERT_LABEL = "EXTRA_ALERT_LABEL"
		const val EXTRA_GUARDIAN_NAME = "EXTRA_GUARDIAN_NAME"
		
		fun startActivity(context: Context, value: String?, label: String, guardianName: String) {
			val intent = Intent(context, AlertValueActivity::class.java)
			intent.putExtra(EXTRA_ALERT_VALUE, value)
			intent.putExtra(EXTRA_GUARDIAN_NAME, guardianName)
			intent.putExtra(EXTRA_ALERT_LABEL, label)
			context.startActivity(intent)
		}
	}
}
