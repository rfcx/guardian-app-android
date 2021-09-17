package org.rfcx.ranger.view.report.create

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_when_investigate_step.*
import org.rfcx.ranger.R
import java.text.DecimalFormat

class SetWhenInvestigateStepFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	private var minutePicker: NumberPicker? = null
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_when_investigate_step, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		nextStepButton.setOnClickListener {
			listener.handleCheckClicked(2)
		}
		
		timePicker.setIs24HourView(true)
		setMinutePicker()
		
		timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
			// get time with ($hourOfDay ${minute * TIME_PICKER_INTERVAL})
		}
	}
	
	
	private fun setMinutePicker() {
		val numValues = 60 / TIME_PICKER_INTERVAL
		val displayedValues = arrayOfNulls<String>(numValues)
		for (i in 0 until numValues) {
			displayedValues[i] = DecimalFormat("00").format(i * TIME_PICKER_INTERVAL)
		}
		
		val minute = timePicker?.findViewById<NumberPicker>(Resources.getSystem().getIdentifier("minute", "id", "android"))
		if (minute != null) {
			minutePicker = minute
			minutePicker!!.minValue = 0
			minutePicker!!.maxValue = numValues - 1
			minutePicker!!.displayedValues = displayedValues
		}
	}
	
	companion object {
		const val TIME_PICKER_INTERVAL = 5
		
		@JvmStatic
		fun newInstance() = SetWhenInvestigateStepFragment()
	}
}
