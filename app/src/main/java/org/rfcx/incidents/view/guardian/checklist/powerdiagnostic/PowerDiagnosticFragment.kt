package org.rfcx.incidents.view.guardian.checklist.powerdiagnostic

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentPowerDiagnosticBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class PowerDiagnosticFragment : Fragment() {

    private lateinit var binding: FragmentPowerDiagnosticBinding
    private val viewModel: PowerDiagnosticViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    private lateinit var powerLineDataSet: LineDataSet

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_power_diagnostic, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle(getString(R.string.power_diagnostic))
        }

        setFeedbackChart()
        setChartDataSetting()

        lifecycleScope.launch {
            viewModel.i2cAccessibilityState.collectLatest {
                if (it.isAccessible) {
                    binding.i2cCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_checklist_passed, 0, 0, 0)
                    binding.i2cCheckTextView.text = getString(R.string.sentinel_module_detect)
                    binding.i2cFailMessage.visibility = View.GONE
                } else {
                    binding.i2cCheckbox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_red_error, 0, 0, 0)
                    binding.i2cCheckTextView.text = getString(R.string.sentinel_module_not_detect)
                    binding.i2cFailMessage.text = it.message
                    binding.i2cFailMessage.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.powerChartState.collectLatest { entry ->
                if (entry != null) {
                    powerLineDataSet = binding.feedbackChart.data.getDataSetByIndex(0) as LineDataSet
                    powerLineDataSet.addEntry(entry)
                    binding.feedbackChart.notifyDataSetChanged()
                    binding.feedbackChart.invalidate()
                }
            }
        }

        binding.nextButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    private fun setFeedbackChart() {
        // setup simple line chart
        binding.feedbackChart.apply {
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.backgroundColor))
            description.isEnabled = false /* description inside chart */
        }

        // set x axis
        binding.feedbackChart.xAxis.apply {
            axisMaximum = X_AXIS_MAXIMUM
            axisMinimum = AXIS_MINIMUM
            axisLineWidth = AXIS_LINE_WIDTH
            position = XAxis.XAxisPosition.BOTTOM
            textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        }

        // set y axis
        binding.feedbackChart.axisLeft.apply {
            axisMaximum = LEFT_AXIS_MAXIMUM
            axisMinimum = AXIS_MINIMUM
            axisLineColor = Color.RED
            axisLineWidth = AXIS_LINE_WIDTH
            textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
        }
    }

    private fun setChartDataSetting() {
        // set line data set
        powerLineDataSet = LineDataSet(arrayListOf<Entry>(), "Power").apply {
            setDrawIcons(false)
            color = Color.RED
            lineWidth = CHART_LINE_WIDTH
            setDrawCircles(false)
            setDrawCircleHole(false)
            formLineWidth = CHART_LINE_WIDTH
            formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            formSize = FORM_SIZE
            valueTextSize = CHART_TEXT_SIZE
            enableDashedHighlightLine(10f, 5f, 0f)
        }

        val dataSets = arrayListOf<ILineDataSet>()
        dataSets.add(powerLineDataSet)

        val lineData = LineData(dataSets)
        binding.feedbackChart.data = lineData
    }

    companion object {
        private const val X_AXIS_MAXIMUM = 100f
        private const val LEFT_AXIS_MAXIMUM = 15000f
        private const val AXIS_MINIMUM = 0f
        private const val AXIS_LINE_WIDTH = 2f

        private const val CHART_LINE_WIDTH = 1f
        private const val FORM_SIZE = 15f
        private const val CHART_TEXT_SIZE = 0f

        @JvmStatic
        fun newInstance() = PowerDiagnosticFragment()
    }
}
