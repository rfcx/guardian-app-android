package org.rfcx.incidents.view.report.create

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_investigation_timestamp.*
import org.rfcx.incidents.R
import org.rfcx.incidents.util.getDay
import org.rfcx.incidents.util.getMonth
import org.rfcx.incidents.util.getYear
import java.text.DecimalFormat
import java.util.*

class InvestigationTimestampFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	private var minutePicker: NumberPicker? = null
	
	private var calendar: Calendar = Calendar.getInstance()
	private var earlier: Calendar = Calendar.getInstance()
	
	private val today = Calendar.getInstance()
	private val yesterday = Calendar.getInstance().also {
		it.add(Calendar.DATE, -1)
	}
	
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
		setupInvestigatedAt()
		timePicker.setIs24HourView(true)
		setMinutePicker()
		setDatePicker()
		setupOnListener()
	}
	
	private fun setupInvestigatedAt() {
		val response = listener.getResponse()
		response?.let { res ->
			calendar.time = res.investigatedAt
			nextStepButton.isEnabled = true
			
			if (DateUtils.isToday(res.investigatedAt.time)) {
				todayRadioButton.isChecked = true
			} else if (calendar.getDay() == yesterday.getDay() && calendar.getMonth() == yesterday.getMonth() && calendar.getYear() == yesterday.getYear()) {
				yesterdayRadioButton.isChecked = true
			} else {
				earlierRadioButton.text = getString(R.string.earlier_date, "${calendar.getDay()}/${calendar.getMonth() + 1}/${calendar.getYear()}")
				earlierRadioButton.isChecked = true
			}
		}
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
					setCalendar(getHour(), getMinute(), today.getDay(), today.getMonth(), today.getYear())
				}
				R.id.yesterdayRadioButton -> {
					setCalendar(getHour(), getMinute(), yesterday.getDay(), yesterday.getMonth(), yesterday.getYear())
				}
				R.id.earlierRadioButton -> {
					setCalendar(getHour(), getMinute(), earlier.getDay(), earlier.getMonth(), earlier.getYear())
				}
			}
		}
	}
	
	private fun setDatePicker() {
		val date = Calendar.getInstance()
		val datePicker = DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->
			earlierRadioButton.text = getString(R.string.earlier_date, "$dayOfMonth/${monthOfYear + 1}/$year")
			earlier.set(Calendar.DAY_OF_MONTH, dayOfMonth)
			earlier.set(Calendar.MONTH, monthOfYear)
			earlier.set(Calendar.YEAR, year)
			setCalendar(getHour(), getMinute(), earlier.getDay(), earlier.getMonth(), earlier.getYear())
		}, date.getYear(), date.getMonth(), date.getDay())
		
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
