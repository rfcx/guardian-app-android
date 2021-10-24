package org.rfcx.ranger.view.report.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_create_report.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.service.ResponseSyncWorker
import org.rfcx.ranger.util.Screen
import java.util.*
import kotlin.collections.ArrayList

class CreateReportActivity : AppCompatActivity(), CreateReportListener {
	
	companion object {
		const val EXTRA_GUARDIAN_NAME = "EXTRA_GUARDIAN_NAME"
		const val EXTRA_GUARDIAN_ID = "EXTRA_GUARDIAN_ID"
		const val EXTRA_RESPONSE_ID = "EXTRA_RESPONSE_ID"
		
		const val RESULT_CODE = 20
		const val EXTRA_SCREEN = "EXTRA_SCREEN"
		
		fun startActivity(context: Context, guardianName: String, guardianId: String, responseId: Int?) {
			val intent = Intent(context, CreateReportActivity::class.java)
			intent.putExtra(EXTRA_GUARDIAN_NAME, guardianName)
			intent.putExtra(EXTRA_GUARDIAN_ID, guardianId)
			intent.putExtra(EXTRA_RESPONSE_ID, responseId)
			context.startActivity(intent)
		}
	}
	
	private val viewModel: CreateReportViewModel by viewModel()
	
	private var passedChecks = ArrayList<Int>()
	private var streamName: String? = null
	private var streamId: String? = null
	private var responseId: Int? = null
	
	private var _response: Response? = null
	private var _images: ArrayList<String> = arrayListOf()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_report)
		streamName = intent?.getStringExtra(EXTRA_GUARDIAN_NAME)
		streamId = intent?.getStringExtra(EXTRA_GUARDIAN_ID)
		responseId = intent?.getIntExtra(EXTRA_RESPONSE_ID, -1)
		
		responseId?.let {
			val response = viewModel.getResponseById(it)
			response?.let { res -> setResponse(res) }
		}
		getImagesFromLocal()
		setupToolbar()
		handleCheckClicked(StepCreateReport.INVESTIGATION_TIMESTAMP.step)
	}
	
	private fun getImagesFromLocal() {
		responseId?.let {
			val images = viewModel.getImagesFromLocal(it)
			images.forEach { reportImage ->
				val path = if (reportImage.remotePath != null) BuildConfig.RANGER_API_DOMAIN + reportImage.remotePath else "file://${reportImage.localPath}"
				_images.add(path)
			}
		}
	}
	
	private fun setupToolbar() {
		setSupportActionBar(toolbarDefault)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			subtitle = streamName
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
	
	override fun getResponse(): Response? = _response
	
	override fun getImages(): ArrayList<String> = _images
	
	override fun setImages(images: ArrayList<String>) {
		_images = images
	}
	
	override fun setAudio(audioPath: String?) {
		val response = _response ?: Response()
		response.audioLocation = audioPath
		setResponse(response)
	}
	
	override fun setInvestigationTimestamp(date: Date) {
		val response = _response ?: Response()
		response.investigatedAt = date
		response.streamId = streamId ?: response.streamId
		response.streamName = streamName ?: response.streamName
		setResponse(response)
	}
	
	override fun setEvidence(evidence: List<Int>) {
		val response = _response ?: Response()
		response.evidences.clear()
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
		response.responseActions.clear()
		response.responseActions.addAll(action)
		setResponse(response)
	}
	
	override fun setNotes(note: String?) {
		val response = _response ?: Response()
		response.note = note
		setResponse(response)
	}
	
	override fun onSaveDraftButtonClick() {
		val response = _response ?: Response()
		viewModel.saveResponseInLocalDb(response, _images)
		
		val intent = Intent()
		intent.putExtra(EXTRA_SCREEN, Screen.DRAFT_REPORTS.id)
		setResult(RESULT_CODE, intent)
		finish()
	}
	
	override fun onSubmitButtonClick() {
		val response = _response ?: Response()
		response.submittedAt = Date()
		viewModel.saveResponseInLocalDb(response, _images)
		viewModel.saveTrackingFile(response, this)
		ResponseSyncWorker.enqueue()
		
		val intent = Intent()
		intent.putExtra(EXTRA_SCREEN, Screen.SUBMITTED_REPORTS.id)
		setResult(RESULT_CODE, intent)
		finish()
	}
	
	private fun startFragment(fragment: Fragment) {
		supportFragmentManager.beginTransaction()
				.replace(createReportContainer.id, fragment)
				.commit()
	}
	
	override fun onBackPressed() {
		when (supportFragmentManager.findFragmentById(R.id.createReportContainer)) {
			is EvidenceFragment -> {
				handleCheckClicked(StepCreateReport.INVESTIGATION_TIMESTAMP.step)
			}
			is ScaleFragment -> {
				handleCheckClicked(StepCreateReport.EVIDENCE.step)
			}
			is DamageFragment -> {
				handleCheckClicked(StepCreateReport.SCALE.step)
			}
			is ActionFragment -> {
				handleCheckClicked(StepCreateReport.DAMAGE.step)
			}
			is AssetsFragment -> {
				handleCheckClicked(StepCreateReport.ACTION.step)
			}
			else -> super.onBackPressed()
		}
	}
}

interface CreateReportListener {
	fun setTitleToolbar(step: Int)
	fun handleCheckClicked(step: Int)
	
	fun getResponse(): Response?
	fun getImages(): ArrayList<String>
	
	fun setInvestigationTimestamp(date: Date)
	fun setEvidence(evidence: List<Int>)
	fun setScale(scale: Int)
	fun setDamage(damage: Int)
	fun setAction(action: List<Int>)
	fun setNotes(note: String?)
	fun setImages(images: ArrayList<String>)
	fun setAudio(audioPath: String?)
	
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
