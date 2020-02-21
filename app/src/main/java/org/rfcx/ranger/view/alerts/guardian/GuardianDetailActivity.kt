package org.rfcx.ranger.view.alerts.guardian

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_guardian_detail.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.alerts.EmptyAlertFragment
import org.rfcx.ranger.view.base.BaseActivity

class GuardianDetailActivity : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_detail)
		setupToolbar()
		
		if (intent.hasExtra(EXTRA_GUARDIAN_NAME) && intent.hasExtra(EXTRA_HAVE_EVENTS)) {
			val guardianName = intent.getStringExtra(EXTRA_GUARDIAN_NAME)
			val haveEvents = intent.getBooleanExtra(EXTRA_HAVE_EVENTS, false)
			
			if (guardianName !== null) {
				if (haveEvents) {
					supportFragmentManager.beginTransaction()
							.replace(guardianListDetailContainer.id,
									GuardianDetailFragment.newInstance(guardianName)).commit()
				} else {
					supportFragmentManager.beginTransaction()
							.replace(guardianListDetailContainer.id, EmptyAlertFragment()).commit()
				}
			}
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			if (intent.hasExtra(EXTRA_GUARDIAN_NAME)) {
				title = intent.getStringExtra(EXTRA_GUARDIAN_NAME)
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		private const val EXTRA_GUARDIAN_NAME = "EXTRA_GUARDIAN_NAME"
		private const val EXTRA_HAVE_EVENTS = "EXTRA_HAVE_EVENTS"
		
		fun startActivity(context: Context, guardianName: String, haveEvents: Boolean) {
			val intent = Intent(context, GuardianDetailActivity::class.java)
			intent.putExtra(EXTRA_GUARDIAN_NAME, guardianName)
			intent.putExtra(EXTRA_HAVE_EVENTS, haveEvents)
			context.startActivity(intent)
		}
	}
}
