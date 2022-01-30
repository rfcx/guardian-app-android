package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_evidence.nextStepButton
import kotlinx.android.synthetic.main.fragment_poaching_evidence.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.PoachingEvidence

class PoachingEvidenceFragment : Fragment() {

    lateinit var listener: CreateReportListener
    private var selected = ArrayList<Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poaching_evidence, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChange()
        setupPoachingEvidence()

        nextStepButton.setOnClickListener {
            setSelect()
        }
    }

    private fun setupPoachingEvidence() {
        val response = listener.getResponse()
        response?.let { res ->
            selected.addAll(res.poachingEvidence)
            nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }

    private fun setOnChange() {
        bulletShellsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        footprintsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        dogTracksCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        otherSpecifyCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        noneCheckBox.setOnClickListener {
            setSelectedNone(true)
            setEnabled()
        }
    }

    private fun setEnabled() {
        selected.clear()
        nextStepButton.isEnabled = bulletShellsCheckBox.isChecked ||
            footprintsCheckBox.isChecked || dogTracksCheckBox.isChecked ||
            otherSpecifyCheckBox.isChecked || noneCheckBox.isChecked
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
        if (noneCheckBox.isChecked) {
            selected.add(PoachingEvidence.NONE.value)
        }
        listener.setPoachingEvidence(selected)
        listener.handleCheckClicked(StepCreateReport.SCALE_POACHING.step)
    }

    private fun setSelectedNone(isNone: Boolean) {
        if (isNone) {
            bulletShellsCheckBox.isChecked = false
            footprintsCheckBox.isChecked = false
            dogTracksCheckBox.isChecked = false
            otherSpecifyCheckBox.isChecked = false
            otherSpecifyCheckBox.isChecked = false
        }
        noneCheckBox.isChecked = isNone
    }

    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                PoachingEvidence.BULLET_SHELLS.value -> bulletShellsCheckBox.isChecked = true
                PoachingEvidence.FOOTPRINTS.value -> footprintsCheckBox.isChecked = true
                PoachingEvidence.DOG_TRACKS.value -> dogTracksCheckBox.isChecked = true
                PoachingEvidence.OTHER.value -> otherSpecifyCheckBox.isChecked = true
                PoachingEvidence.NONE.value -> noneCheckBox.isChecked = true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PoachingEvidenceFragment()
    }
}
