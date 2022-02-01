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
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentInvestigationTimestampBinding
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.getDay
import org.rfcx.incidents.util.getMonth
import org.rfcx.incidents.util.getYear
import org.rfcx.incidents.util.showToast
import org.rfcx.incidents.util.toShortDateString
import java.text.DecimalFormat
import java.util.Calendar

class InvestigationTimestampFragment : Fragment() {
    private var _binding: FragmentInvestigationTimestampBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: CreateReportListener
    private var minutePicker: NumberPicker? = null

    private var calendar: Calendar = Calendar.getInstance()
    private var earlier: Calendar = Calendar.getInstance()

    private val today = Calendar.getInstance()
    private val yesterday = Calendar.getInstance().also {
        it.add(Calendar.DATE, -1)
    }

    private val analytics by lazy { context?.let { Analytics(it) } }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.INVESTIGATION_TIMESTAMP)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvestigationTimestampBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInvestigatedAt()
        binding.timePicker.setIs24HourView(true)
        setMinutePicker()
        setDatePicker()
        setupOnListener()
    }

    private fun setupInvestigatedAt() {
        val response = listener.getResponse()
        response?.let { res ->
            calendar.time = res.investigatedAt
            binding.nextStepButton.isEnabled = true

            if (DateUtils.isToday(res.investigatedAt.time)) {
                binding.todayRadioButton.isChecked = true
            } else if (calendar.getDay() == yesterday.getDay() && calendar.getMonth() == yesterday.getMonth() && calendar.getYear() == yesterday.getYear()) {
                binding.yesterdayRadioButton.isChecked = true
            } else {
                binding.earlierRadioButton.text = calendar.time.toShortDateString()
                binding.earlierRadioButton.isChecked = true
                setShowEdit()
            }
        }
    }

    private fun setupOnListener() {
        binding.nextStepButton.setOnClickListener {
            if (calendar.time > today.time) {
                context?.showToast(getString(R.string.do_not_future_time))
            } else {
                listener.setInvestigationTimestamp(calendar.time)
                listener.handleCheckClicked(StepCreateReport.INVESTIGATION_TYPE.step)
            }
        }

        binding.timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute * TIME_PICKER_INTERVAL)
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.nextStepButton.isEnabled = true

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
            earlier.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            earlier.set(Calendar.MONTH, monthOfYear)
            earlier.set(Calendar.YEAR, year)
            setCalendar(getHour(), getMinute(), earlier.getDay(), earlier.getMonth(), earlier.getYear())
            binding.earlierRadioButton.text = calendar.time.toShortDateString()
            setShowEdit()
        }, date.getYear(), date.getMonth(), date.getDay())

        datePicker.datePicker.maxDate = today.timeInMillis
        datePicker.setOnCancelListener {
            if (binding.earlierRadioButton.text == requireContext().getString(R.string.other_date)) {
                binding.radioGroup.clearCheck()
                binding.nextStepButton.isEnabled = false
            }
            setShowEdit()
        }

        binding.earlierRadioButton.setOnClickListener {
            datePicker.show()
        }
    }

    private fun setShowEdit() {
        binding.editTextView.visibility =
            if (binding.earlierRadioButton.text == requireContext().getString(R.string.other_date)) View.GONE else View.VISIBLE
    }

    private fun setMinutePicker() {
        val numValues = 60 / TIME_PICKER_INTERVAL
        val displayedValues = arrayOfNulls<String>(numValues)
        for (i in 0 until numValues) {
            displayedValues[i] = DecimalFormat("00").format(i * TIME_PICKER_INTERVAL)
        }

        val minute =
            binding.timePicker.findViewById<NumberPicker>(Resources.getSystem().getIdentifier("minute", "id", "android"))
        if (minute != null) {
            minutePicker = minute.also {
                it.minValue = 0
                it.maxValue = numValues - 1
                it.displayedValues = displayedValues
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = calendar.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL
        } else {
            binding.timePicker.currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.currentMinute = calendar.get(Calendar.MINUTE) / TIME_PICKER_INTERVAL
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
        binding.timePicker.minute * TIME_PICKER_INTERVAL
    } else {
        binding.timePicker.currentMinute * TIME_PICKER_INTERVAL
    }

    private fun getHour(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        binding.timePicker.hour
    } else {
        binding.timePicker.currentHour
    }

    companion object {
        const val TIME_PICKER_INTERVAL = 15

        @JvmStatic
        fun newInstance() = InvestigationTimestampFragment()
    }
}
