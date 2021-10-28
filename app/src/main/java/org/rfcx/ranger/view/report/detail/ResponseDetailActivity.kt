package org.rfcx.ranger.view.report.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.toolbar_default.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response

class ResponseDetailActivity : AppCompatActivity() {
	
	companion object {
		const val EXTRA_RESPONSE_CORE_ID = "EXTRA_RESPONSE_CORE_ID"
		
		fun startActivity(context: Context, responseCoreId: String) {
			val intent = Intent(context, ResponseDetailActivity::class.java)
			intent.putExtra(EXTRA_RESPONSE_CORE_ID, responseCoreId)
			context.startActivity(intent)
		}
	}
	
	private val viewModel: ResponseDetailViewModel by viewModel()
	private var responseCoreId: String? = null
	private var response: Response? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_response_detail)
		responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
		response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
		setupToolbar()
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarDefault)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = "#" + response?.incidentRef + " " + response?.streamName
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
}
