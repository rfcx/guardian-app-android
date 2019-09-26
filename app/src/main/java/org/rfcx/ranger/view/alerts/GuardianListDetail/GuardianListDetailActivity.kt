package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_guardian_list_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.base.BaseActivity

class GuardianListDetailActivity : BaseActivity() {
	
	private val guardianListDetailViewModel: GuardianListDetailViewModel by viewModel()
	private val guardianListDetailAdapter by lazy { GuardianListDetailAdapter() }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_guardian_list_detail)
		setupToolbar()
		
		guardianListDetailViewModel.setEventGuid(intent.extras.getString("guid"))
		guardianListDetailViewModel.loadEvantsGuardian()
		
		eventsInGuardianRecycler.apply {
			layoutManager = LinearLayoutManager(this@GuardianListDetailActivity)
			adapter = guardianListDetailAdapter
		}
		
		guardianListDetailViewModel.items.observe(this, Observer { it ->
			it.success({
				loadingProgress.visibility = View.INVISIBLE
				
				if (it.events !== null) {
					guardianListDetailViewModel.makeGroupOfValue(it.events!!)
					guardianListDetailAdapter.allItem = guardianListDetailViewModel.eventAll
				}
				//TODO if event null
			}, {
				loadingProgress.visibility = View.INVISIBLE
				this@GuardianListDetailActivity.handleError(it)
			}, {
				loadingProgress.visibility = View.VISIBLE
			})
		})
		
		guardianListDetailAdapter.mOnItemClickListener = object : OnItemClickEventValuesListener {
			override fun onItemClick(event: MutableList<Event>) {
				Log.d(event[0].value, event.size.toString())
			}
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = intent.extras.getString("value")
		}
	}
	
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	companion object {
		fun startActivity(context: Context, guid: String, value: String) {
			val intent = Intent(context, GuardianListDetailActivity::class.java)
			intent.putExtra("guid", guid)
			intent.putExtra("value", value)
			context.startActivity(intent)
		}
	}
}

interface OnItemClickEventValuesListener {
	fun onItemClick(event: MutableList<Event>)
}
