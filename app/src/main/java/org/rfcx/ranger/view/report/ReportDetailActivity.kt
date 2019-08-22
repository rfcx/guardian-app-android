package org.rfcx.ranger.view.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_report_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseActivity

class ReportDetailActivity : BaseActivity() {
	
	private val viewModel: ReportDetailViewModel by viewModel()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report_detail)
		setupToolbar()
		
		val reportId = intent.getIntExtra(EXTRA_REPORT_ID, -1)
		viewModel.setReport(reportId)
		
		viewModel.getReport().observe(this, Observer {
			reportTypeTextView.text = it?.value ?: "Unknown"
		})
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.report_detail_title)
		}
	}
	
	companion object {
		private const val EXTRA_REPORT_ID = "extra_report_id"
		
		fun startIntent(context: Context?, reportId: Int) {
			context?.let {
				val intent = Intent(it, ReportDetailActivity::class.java)
				intent.putExtra(EXTRA_REPORT_ID, reportId)
				it.startActivity(intent)
			}
		}
	}
}
