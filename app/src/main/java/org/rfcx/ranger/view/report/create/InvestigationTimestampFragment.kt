package org.rfcx.ranger.view.report.create

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_investigation_timestamp.*
import org.rfcx.ranger.R
import java.text.DecimalFormat
import java.util.*

class InvestigationTimestampFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	private var minutePicker: NumberPicker? = null
	
	private var calendar: Calendar = Calendar.getInstance()
	private var earlier: Calendar = Calendar.getInstance()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_investigation_timestamp, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		timePicker.setIs24HourView(true)
		setMinutePicker()
		setDatePicker()
		setupOnListener()
	}
	
	private fun setupOnListener() {
		nextStepButton.setOnClickListener {
			listener.setInvestigationTimestamp(calendar.time)
			listener.handleCheckClicked(StepCreateReport.EVIDENCE.step)
		}
		
		timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
			calendar.set(Calendar.MINUTE, minute * TIME_PICKER_INTERVAL)
		}
		
		radioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.todayRadioButton -> {
					val today = Calendar.getInstance()
					setCalendar(getHour(), getMinute(), today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH), today.get(Calendar.YEAR))
				}
				R.id.yesterdayRadioButton -> {
					val yesterday = Calendar.getInstance().also {
						it.add(Calendar.DATE, -1)
					}
					setCalendar(getHour(), getMinute(), yesterday.get(Calendar.DAY_OF_MONTH), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.YEAR))
				}
				R.id.earlierRadioButton -> {
					setCalendar(getHour(), getMinute(), earlier.get(Calendar.DAY_OF_MONTH), earlier.get(Calendar.MONTH), earlier.get(Calendar.YEAR))
				}
			}
		}
	}
	
	private fun setDatePicker() {
		val date = Calendar.getInstance()
		val datePicker = DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->
			earlierRadioButton.text = getString(R.string.earlier_date, "$dayOfMonth/$monthOfYear/$year")
			earlier.set(Calendar.DAY_OF_MONTH, dayOfMonth)
			earlier.set(Calendar.MONTH, monthOfYear)
			earlier.set(Calendar.YEAR, year)
			setCalendar(getHour(), getMinute(), earlier.get(Calendar.DAY_OF_MONTH), earlier.get(Calendar.MONTH), earlier.get(Calendar.YEAR))
		}, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
		
		earlierRadioButton.setOnClickListener {
			datePicker.show()
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
			minutePicker = minute.also {
				it.minValue = 0
				it.maxValue = numValues - 1
				it.displayedValues = displayedValues
			}
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			timePicker?.hour = calendar.get(Calendar.HOUR_OF_DAY)
			timePicker?.minute = calendar.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL
		} else {
			timePicker?.currentHour = calendar.get(Calendar.HOUR_OF_DAY)
			timePicker?.currentMinute = calendar.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL
		}
	}
	
	private fun setCalendar(hour: Int, minute: Int, dayOfMonth: Int, monthOfYear: Int, year: Int) {
		calendar.set(Calendar.YEAR, year)
		calendar.set(Calendar.HOUR_OF_DAY, hour)
		calendar.set(Calendar.MINUTE, minute)
		calendar.set(Calendar.MONTH, monthOfYear)
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
	}
	
	private fun getMinute(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		timePicker.minute * TIME_PICKER_INTERVAL
	} else {
		timePicker.currentMinute * TIME_PICKER_INTERVAL
	}
	
	private fun getHour(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		timePicker.hour
	} else {
		timePicker.currentHour
	}
	
	companion object {
		const val TIME_PICKER_INTERVAL = 15
		
		@JvmStatic
		fun newInstance() = InvestigationTimestampFragment()
	}
}
