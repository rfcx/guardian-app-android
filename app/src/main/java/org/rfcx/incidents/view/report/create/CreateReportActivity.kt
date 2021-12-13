package org.rfcx.incidents.view.report.create

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_create_report.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.saveToAnswers
import org.rfcx.incidents.service.ResponseSyncWorker
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.util.showToast
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
		
		createReportContainer.setOnTouchListener(object : View.OnTouchListener {
			override fun onTouch(v: View?, event: MotionEvent?): Boolean {
				val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
				if (currentFocus != null) {
					imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
				}
				return true
			}
		})
	}
	
	override fun dispatchTouchEvent(event: MotionEvent): Boolean {
		if (event.action == MotionEvent.ACTION_DOWN) {
			val v = currentFocus
			if (v is EditText) {
				val outRect = Rect()
				v.getGlobalVisibleRect(outRect)
				if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
					v.clearFocus()
					val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
					imm.hideSoftInputFromWindow(v.windowToken, 0)
				}
			}
		}
		return super.dispatchTouchEvent(event)
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
		setSupportActionBar(toolbarLayout)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.create_report_steps)
		}
	}
	
	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}
	
	private fun setSubtitleToolbar(text: String) {
		supportActionBar?.apply {
			subtitle = text
		}
	}
	
	override fun handleCheckClicked(step: Int) {
		passedChecks.add(step)
		setSubtitle(step)
		
		when (step) {
			StepCreateReport.INVESTIGATION_TIMESTAMP.step -> startFragment(InvestigationTimestampFragment.newInstance())
			StepCreateReport.INVESTIGATION_TYPE.step -> startFragment(InvestigationTypeFragment.newInstance())
			StepCreateReport.EVIDENCE.step -> startFragment(EvidenceFragment.newInstance())
			StepCreateReport.SCALE.step -> startFragment(ScaleFragment.newInstance())
			StepCreateReport.POACHING_EVIDENCE.step -> startFragment(PoachingEvidenceFragment.newInstance())
			StepCreateReport.SCALE_POACHING.step -> startFragment(PoachingScaleFragment.newInstance())
			StepCreateReport.ACTION.step -> startFragment(ActionFragment.newInstance())
			StepCreateReport.ASSETS.step -> startFragment(AssetsFragment.newInstance())
		}
	}
	
	private fun setResponse(response: Response) {
		this._response = response
	}
	
	private fun setNumberOnSubtitle(step: Int, isSelectedBoth: Boolean) {
		when (step) {
			StepCreateReport.EVIDENCE.step -> if (isSelectedBoth) setSubtitleToolbar(getString(R.string.create_report_subtitle, 5)) else setSubtitleToolbar(getString(R.string.create_report_subtitle, 3))
			StepCreateReport.SCALE.step -> if (isSelectedBoth) setSubtitleToolbar(getString(R.string.create_report_subtitle, 4)) else setSubtitleToolbar(getString(R.string.create_report_subtitle, 2))
			StepCreateReport.POACHING_EVIDENCE.step -> setSubtitleToolbar(getString(R.string.create_report_subtitle, 3))
			StepCreateReport.SCALE_POACHING.step -> setSubtitleToolbar(getString(R.string.create_report_subtitle, 2))
			StepCreateReport.ACTION.step -> setSubtitleToolbar(getString(R.string.create_report_subtitle_one_step))
		}
	}
	
	private fun setSubtitle(step: Int) {
		val response = _response ?: Response()
		if (response.investigateType.contains(InvestigationType.POACHING.value) && response.investigateType.contains(InvestigationType.LOGGING.value)) {
			setNumberOnSubtitle(step, true)
		} else {
			setNumberOnSubtitle(step, false)
		}
		
		if (step == StepCreateReport.ASSETS.step) {
			setSubtitleToolbar(getString(R.string.last_step))
		}
		if (step == StepCreateReport.INVESTIGATION_TIMESTAMP.step || step == StepCreateReport.INVESTIGATION_TYPE.step) {
			setSubtitleToolbar("")
		}
	}
	
	override fun getResponse(): Response? = _response
	
	override fun getImages(): ArrayList<String> = _images
	
	override fun getSiteName(): String? = streamName
	
	override fun setImages(images: ArrayList<String>) {
		_images = images
	}
	
	override fun setAudio(audioPath: String?) {
		val response = _response ?: Response()
		response.audioLocation = audioPath
		setResponse(response)
	}
	
	override fun setInvestigateType(type: ArrayList<Int>) {
		val response = _response ?: Response()
		response.investigateType.clear()
		response.investigateType.addAll(type)
		setResponse(response)
	}
	
	override fun setPoachingScale(poachingScale: Int) {
		val response = _response ?: Response()
		response.poachingScale = poachingScale
		setResponse(response)
	}
	
	override fun setPoachingEvidence(poachingEvidence: List<Int>) {
		val response = _response ?: Response()
		response.poachingEvidence.clear()
		response.poachingEvidence.addAll(poachingEvidence)
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
		response.answers = response.saveToAnswers()
		viewModel.saveResponseInLocalDb(response, _images)
		viewModel.saveTrackingFile(response, this)
		when {
			this.isOnAirplaneMode() -> {
				this.showToast(getString(R.string.unable_to_submit_on_airplane_mode) + " " + getString(R.string.pls_off_air_plane_mode))
			}
			!this.isNetworkAvailable() -> {
				this.showToast(getString(R.string.unable_to_submit_no_internet_connection))
			}
			else -> {
				ResponseSyncWorker.enqueue()
			}
		}
		
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
			is InvestigationTypeFragment -> {
				handleCheckClicked(StepCreateReport.INVESTIGATION_TIMESTAMP.step)
			}
			is EvidenceFragment -> {
				handleCheckClicked(StepCreateReport.INVESTIGATION_TYPE.step)
			}
			is PoachingEvidenceFragment -> {
				val response = _response ?: Response()
				if (response.investigateType.contains(InvestigationType.LOGGING.value)) {
					handleCheckClicked(StepCreateReport.SCALE.step)
				} else {
					handleCheckClicked(StepCreateReport.INVESTIGATION_TYPE.step)
				}
			}
			is ScaleFragment -> {
				handleCheckClicked(StepCreateReport.EVIDENCE.step)
			}
			is PoachingScaleFragment -> {
				handleCheckClicked(StepCreateReport.POACHING_EVIDENCE.step)
			}
			is ActionFragment -> {
				val response = _response ?: Response()
				if (response.investigateType.contains(InvestigationType.LOGGING.value)) {
					if (response.investigateType.contains(InvestigationType.POACHING.value)) {
						handleCheckClicked(StepCreateReport.SCALE_POACHING.step)
					} else {
						handleCheckClicked(StepCreateReport.SCALE.step)
					}
				} else if (response.investigateType.contains(InvestigationType.POACHING.value)) {
					handleCheckClicked(StepCreateReport.SCALE_POACHING.step)
				} else {
					handleCheckClicked(StepCreateReport.INVESTIGATION_TYPE.step)
				}
			}
			is AssetsFragment -> {
				val response = _response ?: Response()
				if (response.investigateType.contains(InvestigationType.OTHER.value)) {
					handleCheckClicked(StepCreateReport.INVESTIGATION_TYPE.step)
				} else {
					handleCheckClicked(StepCreateReport.ACTION.step)
				}
			}
			else -> super.onBackPressed()
		}
	}
}

interface CreateReportListener {
	fun handleCheckClicked(step: Int)
	
	fun getResponse(): Response?
	fun getImages(): ArrayList<String>
	fun getSiteName(): String?
	
	fun setInvestigationTimestamp(date: Date)
	fun setEvidence(evidence: List<Int>)
	fun setScale(scale: Int)
	fun setDamage(damage: Int)
	fun setAction(action: List<Int>)
	fun setNotes(note: String?)
	fun setImages(images: ArrayList<String>)
	fun setAudio(audioPath: String?)
	fun setInvestigateType(type: ArrayList<Int>)
	fun setPoachingScale(poachingScale: Int)
	fun setPoachingEvidence(poachingEvidence: List<Int>)
	
	fun onSaveDraftButtonClick()
	fun onSubmitButtonClick()
}

enum class StepCreateReport(val step: Int) {
	INVESTIGATION_TIMESTAMP(1),
	INVESTIGATION_TYPE(2),
	EVIDENCE(3),
	SCALE(4),
	POACHING_EVIDENCE(5),
	SCALE_POACHING(6),
	ACTION(7),
	ASSETS(8),
	
	
	DAMAGE(9),
}
