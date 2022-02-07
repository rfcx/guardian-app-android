package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.FragmentActionBinding
import org.rfcx.incidents.entity.response.Actions
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class ActionFragment : Fragment() {
    private var _binding: FragmentActionBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: CreateReportListener
    private var selected = ArrayList<Int>()
    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.ACTION)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupResponseActions()
        setOnChange()

        binding.nextStepButton.setOnClickListener {
            setSelect()
        }
    }

    private fun setupResponseActions() {
        val response = listener.getResponse()
        response?.let { res ->
            selected.addAll(res.responseActions)
            binding.nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }

    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                Actions.COLLECTED_EVIDENCE.value -> binding.collectedCheckBox.isChecked = true
                Actions.ISSUE_A_WARNING.value -> binding.warningCheckBox.isChecked = true
                Actions.CONFISCATED_EQUIPMENT.value -> binding.confiscatedCheckBox.isChecked = true
                Actions.DAMAGED_MACHINERY.value -> binding.damagedMachineryCheckBox.isChecked = true
            }
        }
    }

    private fun setOnChange() {
        binding.collectedCheckBox.setOnClickListener {
            setEnabled()
        }
        binding.warningCheckBox.setOnClickListener {
            setEnabled()
        }
        binding.confiscatedCheckBox.setOnClickListener {
            setEnabled()
        }
        binding.damagedMachineryCheckBox.setOnClickListener {
            setEnabled()
        }
    }

    private fun setEnabled() {
        selected.clear()
        binding.nextStepButton.isEnabled = binding.collectedCheckBox.isChecked ||
            binding.warningCheckBox.isChecked || binding.confiscatedCheckBox.isChecked ||
            binding.damagedMachineryCheckBox.isChecked
    }

    private fun setSelect() {
        selected.clear()
        if (binding.collectedCheckBox.isChecked) {
            selected.add(Actions.COLLECTED_EVIDENCE.value)
        }
        if (binding.warningCheckBox.isChecked) {
            selected.add(Actions.ISSUE_A_WARNING.value)
        }
        if (binding.confiscatedCheckBox.isChecked) {
            selected.add(Actions.CONFISCATED_EQUIPMENT.value)
        }
        if (binding.damagedMachineryCheckBox.isChecked) {
            selected.add(Actions.DAMAGED_MACHINERY.value)
        }
        listener.setAction(selected)
        listener.handleCheckClicked(StepCreateReport.ASSETS.step)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = ActionFragment()
    }
}
