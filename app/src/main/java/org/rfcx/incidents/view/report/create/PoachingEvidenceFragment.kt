package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_evidence.nextStepButton
import kotlinx.android.synthetic.main.fragment_evidence.siteNameTextView
import kotlinx.android.synthetic.main.fragment_poaching_evidence.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.PoachingEvidence
import java.util.*

class PoachingEvidenceFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	private var selected = ArrayList<Int>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_poaching_evidence, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		siteNameTextView.text = getString(R.string.site_name, listener.getSiteName())
		setOnChange()
		
		nextStepButton.setOnClickListener {
			setSelect()
		}
	}
	
	private fun setOnChange() {
		bulletShellsCheckBox.setOnClickListener {
			setSelectedOther(false)
			setEnabled()
		}
		footprintsCheckBox.setOnClickListener {
			setSelectedOther(false)
			setEnabled()
		}
		dogTracksCheckBox.setOnClickListener {
			setSelectedOther(false)
			setEnabled()
		}
		otherSpecifyCheckBox.setOnClickListener {
			setSelectedOther(true)
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		selected.clear()
		nextStepButton.isEnabled = bulletShellsCheckBox.isChecked ||
				footprintsCheckBox.isChecked || dogTracksCheckBox.isChecked ||
				otherSpecifyCheckBox.isChecked
	}
	
	private fun setSelect() {
		selected.clear()
		if (bulletShellsCheckBox.isChecked) {
			selected.add(PoachingEvidence.BULLET_SHELLS.value)
		}
		if (footprintsCheckBox.isChecked) {
			selected.add(PoachingEvidence.FOOTPRINTS.value)
		}
		if (dogTracksCheckBox.isChecked) {
			selected.add(PoachingEvidence.DOG_TRACKS.value)
		}
		if (otherSpecifyCheckBox.isChecked) {
			selected.add(PoachingEvidence.OTHER.value)
		}
		
		listener.handleCheckClicked(StepCreateReport.SCALE_POACHING.step) // change
	}
	
	private fun setSelectedOther(isOther: Boolean) {
		if (isOther) {
			bulletShellsCheckBox.isChecked = !isOther
			footprintsCheckBox.isChecked = !isOther
			dogTracksCheckBox.isChecked = !isOther
			otherSpecifyCheckBox.isChecked = !isOther
		}
		otherSpecifyCheckBox.isChecked = isOther
	}
	
	private fun setSelected() {
		selected.forEach { id ->
			if (id == PoachingEvidence.BULLET_SHELLS.value) {
				bulletShellsCheckBox.isChecked = true
			}
			if (id == PoachingEvidence.FOOTPRINTS.value) {
				footprintsCheckBox.isChecked = true
			}
			if (id == PoachingEvidence.DOG_TRACKS.value) {
				dogTracksCheckBox.isChecked = true
			}
			if (id == PoachingEvidence.OTHER.value) {
				otherSpecifyCheckBox.isChecked = true
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = PoachingEvidenceFragment()
	}
}
