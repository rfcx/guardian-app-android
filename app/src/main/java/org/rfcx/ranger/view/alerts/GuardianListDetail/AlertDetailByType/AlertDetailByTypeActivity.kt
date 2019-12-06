package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alert_detail_by_type.*
import kotlinx.android.synthetic.main.fragment_guardian_list_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError

class AlertDetailByTypeActivity : AppCompatActivity() {
	private val viewModel: AlertDetailByTypeViewModel by viewModel()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_alert_detail_by_type)
		setupToolbar()
		
		if (intent?.hasExtra("ALERT_VALUE") == true) {
			val value = intent.getStringExtra("ALERT_VALUE")
			if (value != null) {
				viewModel.getEventFromDatabase(value)
			}
		}
		
		viewModel.arrayEvent.observe(this, Observer {
			it.success({ items ->
				items.forEach {
					Log.d("items","items size ${items.size}")
				}
//				loadingProgress.visibility = View.INVISIBLE
//				guardianListDetailAdapter.allItem = items
			}, {
//				loadingProgress.visibility = View.INVISIBLE
//				context.handleError(it)
			}, {
//				loadingProgress.visibility = View.VISIBLE
			})
		})
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarForType)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			if (intent?.hasExtra("GUARDIAN_NAME") == true) {
				title = "${intent.getStringExtra("GUARDIAN_NAME")} - ${intent.getStringExtra("ALERT_VALUE")}"
			}
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context, value: String, guardianName: String) {
			val intent = Intent(context, AlertDetailByTypeActivity::class.java)
			intent.putExtra("ALERT_VALUE", value)
			intent.putExtra("GUARDIAN_NAME", guardianName)
			context.startActivity(intent)
		}
	}
}
