package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentPoachingScaleBinding
import org.rfcx.incidents.entity.response.PoachingScale

class PoachingScaleFragment : Fragment() {
    private var _binding: FragmentPoachingScaleBinding? = null
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
        _binding = FragmentPoachingScaleBinding.inflate(inflater, container, false)
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
                listener.setPoachingScale(value)
                listener.handleCheckClicked(StepCreateReport.ACTION.step)
            }
        }

        binding.scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.nextStepButton.isEnabled = true

            when (checkedId) {
                R.id.smallRadioButton -> selected = PoachingScale.SMALL.value
                R.id.largeRadioButton -> selected = PoachingScale.LARGE.value
                R.id.noneRadioButton -> selected = PoachingScale.NONE.value
            }
        }
    }

    private fun setupScale() {
        val response = listener.getResponse()
        response?.let { res ->
            selected = res.poachingScale
            binding.nextStepButton.isEnabled = selected != PoachingScale.DEFAULT.value
            when (selected) {
                PoachingScale.SMALL.value -> binding.smallRadioButton.isChecked = true
                PoachingScale.LARGE.value -> binding.largeRadioButton.isChecked = true
                PoachingScale.NONE.value -> binding.noneRadioButton.isChecked = true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PoachingScaleFragment()
    }
}
