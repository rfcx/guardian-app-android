package org.rfcx.ranger.view.report.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_create_report.*
import kotlinx.android.synthetic.main.toolbar_default.*
import org.rfcx.ranger.R
import java.util.*

class CreateReportActivity : AppCompatActivity(), CreateReportListener {
	private var guardianName: String? = null
	
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
			1 -> startFragment(StepOneFragment.newInstance())
			2 -> startFragment(StepTwoFragment.newInstance())
			3 -> startFragment(StepThreeFragment.newInstance())
			4 -> startFragment(StepFourFragment.newInstance())
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

