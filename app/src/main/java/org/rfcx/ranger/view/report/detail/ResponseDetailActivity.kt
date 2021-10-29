package org.rfcx.ranger.view.report.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_response_detail.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.*
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo

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
	private val responseDetailAdapter by lazy { ResponseDetailAdapter() }
	
	private var responseCoreId: String? = null
	private var response: Response? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_response_detail)
		responseCoreId = intent?.getStringExtra(EXTRA_RESPONSE_CORE_ID)
		response = responseCoreId?.let { viewModel.getResponseByCoreId(it) }
		setupToolbar()
		
		answersRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = responseDetailAdapter
		}
		
		response?.let { res ->
			investigateAtTextView.text = res.investigatedAt.toTimeSinceStringAlternativeTimeAgo(this)
			responseDetailAdapter.items = getMessageList(res.answers)
			noteTextView.visibility = if (res.note != null) View.VISIBLE else View.GONE
			noteTextView.text =  getString(R.string.note, res.note)
		}
	}
	
	private fun getMessageList(answers: List<Int>): List<String> {
		val messageList = arrayListOf<String>()
		answers.forEach { id ->
			id.getMessage()?.let { msg -> messageList.add(msg) }
		}
		return messageList
	}
	
	private fun Int.getMessage(): String? {
		return when {
			// LoggingScale
			this == LoggingScale.LARGE.value -> {
				getString(R.string.logging_scale) + " " + getString(R.string.large_text)
			}
			this == LoggingScale.SMALL.value -> {
				getString(R.string.logging_scale) + " " + getString(R.string.small_text)
			}
			
			// DamageScale
			this == DamageScale.NO_VISIBLE.value -> {
				getString(R.string.damage) + " " + getString(R.string.no_visible)
			}
			this == DamageScale.SMALL.value -> {
				getString(R.string.damage) + " " + getString(R.string.small_trees_cut_down)
			}
			this == DamageScale.MEDIUM.value -> {
				getString(R.string.damage) + " " + getString(R.string.medium_trees_cut_down)
			}
			this == DamageScale.LARGE.value -> {
				getString(R.string.damage) + " " + getString(R.string.large_area_clear_cut)
			}
			
			// EvidenceTypes
			this == EvidenceTypes.CUT_DOWN_TREES.value -> {
				getString(R.string.cut_down_trees)
			}
			this == EvidenceTypes.CLEARED_AREAS.value -> {
				getString(R.string.cleared_areas)
			}
			this == EvidenceTypes.LOGGING_EQUIPMENT.value -> {
				getString(R.string.logging_equipment)
			}
			this == EvidenceTypes.LOGGERS_AT_SITE.value -> {
				getString(R.string.loggers_at_site)
			}
			this == EvidenceTypes.ILLEGAL_CAMPS.value -> {
				getString(R.string.illegal_camps)
			}
			this == EvidenceTypes.FIRED_BURNED_AREAS.value -> {
				getString(R.string.fires_burned_areas)
			}
			this == EvidenceTypes.EVIDENCE_OF_POACHING.value -> {
				getString(R.string.evidence_of_poaching)
			}
			
			// Actions
			this == Actions.COLLECTED_EVIDENCE.value -> {
				getString(R.string.collected_evidence)
			}
			this == Actions.ISSUE_A_WARNING.value -> {
				getString(R.string.issue_a_warning)
			}
			this == Actions.CONFISCATED_EQUIPMENT.value -> {
				getString(R.string.confiscated_equipment)
			}
			this == Actions.ARRESTS.value -> {
				getString(R.string.arrests)
			}
			this == Actions.PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT.value -> {
				getString(R.string.planning_security)
			}
			else -> null
		}
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
