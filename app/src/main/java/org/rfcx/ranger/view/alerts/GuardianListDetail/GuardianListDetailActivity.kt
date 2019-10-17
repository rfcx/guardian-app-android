package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_guardian_list_detail.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.EmptyAlertFragment
import org.rfcx.ranger.view.alerts.adapter.EventItem
import org.rfcx.ranger.view.base.BaseActivity

class GuardianListDetailActivity : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_list_detail)
		setupToolbar()
		
		if (intent?.hasExtra("event") == true) {
			val event = intent.getParcelableArrayListExtra<Event>("event")
			Log.d("GuardianListDetail", "$event")
			
			supportFragmentManager.beginTransaction()
					.replace(guardianListDetailContainer.id, GuardianListDetailFragment.newInstance(event),
							"GuardianListDetailFragment").commit()
		} else {
			supportFragmentManager.beginTransaction()
					.replace(guardianListDetailContainer.id, EmptyAlertFragment(),
							"EmptyAlertFragment").commit()
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			if (intent?.hasExtra("name") == true) {
				title = intent.getStringExtra("name")
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context, event: List<Event>, name: String) {
			val intent = Intent(context, GuardianListDetailActivity::class.java)
			intent.putParcelableArrayListExtra("event", ArrayList(event))
			intent.putExtra("name", name)
			context.startActivity(intent)
		}
	}
}

interface OnItemClickEventValuesListener {
	fun onItemClick(event: MutableList<EventItem>)
}
