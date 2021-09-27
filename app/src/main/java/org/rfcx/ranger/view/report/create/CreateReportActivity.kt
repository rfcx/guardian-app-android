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
import java.util.*

class CreateReportActivity : AppCompatActivity(), CreateReportListener {
	private var guardianName: String? = null
	private val viewModel: CreateReportViewModel by viewModel()
	
	companion object {
		private const val EXTRA_GUARDIAN_NAME = "EXTRA_GUARDIAN_NAME"
		
		fun startActivity(context: Context, guardianName: String) {
			val intent = Intent(context, CreateReportActivity::class.java)
			intent.putExtra(EXTRA_GUARDIAN_NAME, guardianName)
			context.startActivity(intent)
		}
	}
	
	private var passedChecks = ArrayList<Int>()
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_report)
		guardianName = intent?.getStringExtra(EXTRA_GUARDIAN_NAME)
		
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
//			StepCreateReport.INVESTIGATION_TIMESTAMP.step -> startFragment(InvestigationTimestampFragment.newInstance())
			StepCreateReport.INVESTIGATION_TIMESTAMP.step -> viewModel.createResponse()
			StepCreateReport.EVIDENCE.step -> startFragment(EvidenceFragment.newInstance())
			StepCreateReport.SCALE.step -> startFragment(ScaleFragment.newInstance())
			StepCreateReport.DAMAGE.step -> startFragment(DamageFragment.newInstance())
			StepCreateReport.ACTION.step -> startFragment(ActionFragment.newInstance())
			StepCreateReport.ASSETS.step -> startFragment(AssetsFragment.newInstance())
		}
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
}

enum class StepCreateReport(val step: Int) {
	INVESTIGATION_TIMESTAMP(1),
	EVIDENCE(2),
	SCALE(3),
	DAMAGE(4),
	ACTION(5),
	ASSETS(6)
}
