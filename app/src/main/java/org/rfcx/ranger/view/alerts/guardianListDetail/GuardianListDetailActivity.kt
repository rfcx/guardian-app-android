package org.rfcx.ranger.view.alerts.guardianListDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_guardian_list_detail.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.alerts.EmptyAlertFragment
import org.rfcx.ranger.view.base.BaseActivity

class GuardianListDetailActivity : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_list_detail)
		setupToolbar()
		
		if (intent?.hasExtra("GUARDIAN_NAME") == true && intent?.hasExtra("HAVE_EVENTS") == true) {
			val guardianName = intent.getStringExtra("GUARDIAN_NAME")
			val haveEvents = intent.getBooleanExtra("HAVE_EVENTS", false)
			
			if (guardianName !== null) {
				if (haveEvents) {
					supportFragmentManager.beginTransaction()
							.replace(guardianListDetailContainer.id, GuardianListDetailFragment.newInstance(guardianName),
									"GuardianListDetailFragment").commit()
				} else {
					supportFragmentManager.beginTransaction()
							.replace(guardianListDetailContainer.id, EmptyAlertFragment(),
									"EmptyAlertFragment").commit()
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
			if (intent?.hasExtra("GUARDIAN_NAME") == true) {
				title = intent.getStringExtra("GUARDIAN_NAME")
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context, guardianName: String, haveEvents: Boolean) {
			val intent = Intent(context, GuardianListDetailActivity::class.java)
			intent.putExtra("GUARDIAN_NAME", guardianName)
			intent.putExtra("HAVE_EVENTS", haveEvents)
			context.startActivity(intent)
		}
	}
}