package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_poaching_scale.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.PoachingScale

class PoachingScaleFragment : Fragment() {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_poaching_scale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupScale()

        nextStepButton.setOnClickListener {
            selected?.let { value ->
                listener.setPoachingScale(value)
                listener.handleCheckClicked(StepCreateReport.ACTION.step)
            }
        }

        scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            nextStepButton.isEnabled = true

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
            nextStepButton.isEnabled = selected != PoachingScale.DEFAULT.value
            when (selected) {
                PoachingScale.SMALL.value -> smallRadioButton.isChecked = true
                PoachingScale.LARGE.value -> largeRadioButton.isChecked = true
                PoachingScale.NONE.value -> noneRadioButton.isChecked = true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PoachingScaleFragment()
    }
}
