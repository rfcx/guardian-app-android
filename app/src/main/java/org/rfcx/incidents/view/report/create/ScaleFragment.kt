package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentScaleBinding
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.entity.response.LoggingScale

class ScaleFragment : Fragment() {
    private var _binding: FragmentScaleBinding? = null
    private val binding get() = _binding!!

    lateinit var listener: CreateReportListener
    var selected: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScale()

        binding.nextStepButton.setOnClickListener {
            selected?.let { value ->
                listener.setScale(value)
                val response = listener.getResponse()

                response?.let {
                    if (it.investigateType.contains(InvestigationType.POACHING.value)) {
                        listener.handleCheckClicked(StepCreateReport.POACHING_EVIDENCE.step)
                    } else {
                        listener.handleCheckClicked(StepCreateReport.ACTION.step)
                    }
                }
            }
        }

        binding.scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.nextStepButton.isEnabled = true

            when (checkedId) {
                R.id.smallRadioButton -> selected = LoggingScale.SMALL.value
                R.id.largeRadioButton -> selected = LoggingScale.LARGE.value
                R.id.noneRadioButton -> selected = LoggingScale.NONE.value
            }
        }
    }

    private fun setupScale() {
        val response = listener.getResponse()
        response?.let { res ->
            selected = res.loggingScale
            binding.nextStepButton.isEnabled = selected != LoggingScale.DEFAULT.value
            when (selected) {
                LoggingScale.SMALL.value -> binding.smallRadioButton.isChecked = true
                LoggingScale.LARGE.value -> binding.largeRadioButton.isChecked = true
                LoggingScale.NONE.value -> binding.noneRadioButton.isChecked = true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ScaleFragment()
    }
}
