package org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_detail_by_type.*
import org.rfcx.ranger.R

class AlertDetailByTypeActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_alert_detail_by_type)
		setupToolbar()
		
		if (intent?.hasExtra(ALERT_VALUE) == true) {
			val value = intent.getStringExtra(ALERT_VALUE)
			if (value != null) {
				supportFragmentManager.beginTransaction()
						.replace(alertDetailByTypeContainer.id, AlertDetailByTypeFragment.newInstance(value),
								"AlertDetailByTypeFragment").commit()
			}
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarForType)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			if (intent?.hasExtra(GUARDIAN_NAME) == true) {
				title = "${intent.getStringExtra(GUARDIAN_NAME)} - ${intent.getStringExtra(ALERT_VALUE).capitalize()}"
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		const val ALERT_VALUE = "ALERT_VALUE"
		const val GUARDIAN_NAME = "GUARDIAN_NAME"
		
		fun startActivity(context: Context, value: String, guardianName: String) {
			val intent = Intent(context, AlertDetailByTypeActivity::class.java)
			intent.putExtra(ALERT_VALUE, value)
			intent.putExtra(GUARDIAN_NAME, guardianName)
			context.startActivity(intent)
		}
	}
}
