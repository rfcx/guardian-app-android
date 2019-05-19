package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_report_list.*
import org.rfcx.ranger.R

class ReportListActivity : AppCompatActivity() {
	
	companion object {
		fun startIntent(context: Context?) {
			context?.startActivity(Intent(context, ReportListActivity::class.java))
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report_list)
		setupToolbar()
		
		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
					.replace(reportListContainer.id,
							ReportListFragment.newInstance(),
							ReportListFragment.tag).commit()
		}
		
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.report_list_title)
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
}