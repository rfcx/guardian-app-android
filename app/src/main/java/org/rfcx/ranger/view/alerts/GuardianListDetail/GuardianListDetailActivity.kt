package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_guardian_list_detail.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.base.BaseActivity

class GuardianListDetailActivity : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_list_detail)
		setupToolbar()
		
		supportFragmentManager.beginTransaction()
				.replace(guardianListDetailContainer.id, GuardianListDetailFragment(),
						"GuardianListDetailFragment").commit()
		
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = "Test"
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, GuardianListDetailActivity::class.java)
			context.startActivity(intent)
		}
	}
}

interface OnItemClickEventValuesListener {
	fun onItemClick(event: MutableList<Event>)
}
