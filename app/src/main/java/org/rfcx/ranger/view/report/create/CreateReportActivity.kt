package org.rfcx.ranger.view.report.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_create_report.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response
import java.util.*
import kotlin.collections.ArrayList

class CreateReportActivity : AppCompatActivity(), CreateReportListener {
	
	companion object {
		private const val EXTRA_GUARDIAN_NAME = "EXTRA_GUARDIAN_NAME"
		private const val EXTRA_GUARDIAN_ID = "EXTRA_GUARDIAN_ID"
		
		fun startActivity(context: Context, guardianName: String, guardianId: String) {
			val intent = Intent(context, CreateReportActivity::class.java)
			intent.putExtra(EXTRA_GUARDIAN_NAME, guardianName)
			intent.putExtra(EXTRA_GUARDIAN_ID, guardianId)
			context.startActivity(intent)
		}
	}
	
	private val viewModel: CreateReportViewModel by viewModel()
	
	private var passedChecks = ArrayList<Int>()
	private var guardianName: String? = null
	private var guardianId: String? = null
	
	private var _response: Response? = null
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_report)
		guardianName = intent?.getStringExtra(EXTRA_GUARDIAN_NAME)
		guardianId = intent?.getStringExtra(EXTRA_GUARDIAN_ID)
		
		setupToolbar()
		handleCheckClicked(1)
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarDefault)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			subtitle = guardianName
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	override fun setTitleToolbar(step: Int) {
		supportActionBar?.apply {
			title = getString(R.string.create_report_steps, step)
		}
	}
	
	override fun handleCheckClicked(step: Int) {
		passedChecks.add(step)
		setTitleToolbar(step)
		
		when (step) {
			StepCreateReport.INVESTIGATION_TIMESTAMP.step -> startFragment(InvestigationTimestampFragment.newInstance())
			StepCreateReport.EVIDENCE.step -> startFragment(EvidenceFragment.newInstance())
			StepCreateReport.SCALE.step -> startFragment(ScaleFragment.newInstance())
			StepCreateReport.DAMAGE.step -> startFragment(DamageFragment.newInstance())
			StepCreateReport.ACTION.step -> startFragment(ActionFragment.newInstance())
			StepCreateReport.ASSETS.step -> startFragment(AssetsFragment.newInstance())
		}
	}
	
	private fun setResponse(response: Response) {
		this._response = response
	}
	
	override fun setInvestigationTimestamp(date: Date) {
		val response = _response ?: Response()
		response.investigatedAt = date
		response.guardianId = guardianId ?: ""
		setResponse(response)
	}
	
	override fun setEvidence(evidence: List<Int>) {
		val response = _response ?: Response()
		response.evidences.addAll(evidence)
		setResponse(response)
	}
	
	override fun setScale(scale: Int) {
		val response = _response ?: Response()
		response.loggingScale = scale
		setResponse(response)
	}
	
	override fun setDamage(damage: Int) {
		val response = _response ?: Response()
		response.damageScale = damage
		setResponse(response)
	}
	
	override fun setAction(action: List<Int>) {
		val response = _response ?: Response()
		response.responseActions.addAll(action)
		setResponse(response)
	}
	
	override fun setAssets(note: String) {
		val response = _response ?: Response()
		response.note = note
		setResponse(response)
		finish()
	}
	
	override fun onSaveDraftButtonClick() {
		val response = _response ?: Response()
		viewModel.saveResponseInLocalDb(response)
	}
	
	override fun onSubmitButtonClick() {
		val response = _response ?: Response()
		response.submittedAt = Date()
		viewModel.saveResponseInLocalDb(response)
		viewModel.createResponse(response)
	}
	
	private fun startFragment(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
				.replace(createReportContainer.id, fragment)
				.commit()
	}
}

interface CreateReportListener {
	fun setTitleToolbar(step: Int)
	fun handleCheckClicked(step: Int)
	
	fun setInvestigationTimestamp(date: Date)
	fun setEvidence(evidence: List<Int>)
	fun setScale(scale: Int)
	fun setDamage(damage: Int)
	fun setAction(action: List<Int>)
	fun setAssets(note: String)
	
	fun onSaveDraftButtonClick()
	fun onSubmitButtonClick()
}

enum class StepCreateReport(val step: Int) {
	INVESTIGATION_TIMESTAMP(1),
	EVIDENCE(2),
	SCALE(3),
	DAMAGE(4),
	ACTION(5),
	ASSETS(6)
}
