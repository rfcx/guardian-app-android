package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.FragmentInvestigationTypeBinding
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class InvestigationTypeFragment : Fragment() {
    private var _binding: FragmentInvestigationTypeBinding? = null
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
        analytics?.trackScreen(Screen.INVESTIGATION_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestigationTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChange()
        setupInvestigateType()

        binding.nextStepButton.setOnClickListener {
            selected.clear()
            if (binding.loggingCheckBox.isChecked) {
                selected.add(InvestigationType.LOGGING.value)
            }
            if (binding.poachingCheckBox.isChecked) {
                selected.add(InvestigationType.POACHING.value)
            }
            if (binding.otherCheckBox.isChecked) {
                selected.add(InvestigationType.OTHER.value)
            }
            listener.setInvestigateType(selected)

            when {
                selected.contains(InvestigationType.LOGGING.value) -> {
                    listener.handleCheckClicked(StepCreateReport.EVIDENCE.step)
                }
                selected.contains(InvestigationType.POACHING.value) -> {
                    listener.handleCheckClicked(StepCreateReport.POACHING_EVIDENCE.step)
                }
                selected.contains(InvestigationType.OTHER.value) -> {
                    listener.handleCheckClicked(StepCreateReport.ASSETS.step)
                }
            }
        }
    }

    private fun setupInvestigateType() {
        val response = listener.getResponse()
        response?.let { res ->
            selected.addAll(res.investigateType)
            binding.nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }

    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                InvestigationType.LOGGING.value -> binding.loggingCheckBox.isChecked = true
                InvestigationType.POACHING.value -> binding.poachingCheckBox.isChecked = true
                InvestigationType.OTHER.value -> binding.otherCheckBox.isChecked = true
            }
        }
    }

    private fun setOnChange() {
        binding.loggingCheckBox.setOnClickListener {
            setEnabled()
        }
        binding.poachingCheckBox.setOnClickListener {
            setEnabled()
        }
        binding.otherCheckBox.setOnClickListener {
            setEnabled()
        }
    }

    private fun setEnabled() {
        selected.clear()
        binding.nextStepButton.isEnabled = binding.loggingCheckBox.isChecked ||
            binding.poachingCheckBox.isChecked || binding.otherCheckBox.isChecked
    }

    companion object {
        @JvmStatic
        fun newInstance() = InvestigationTypeFragment()
    }
}
