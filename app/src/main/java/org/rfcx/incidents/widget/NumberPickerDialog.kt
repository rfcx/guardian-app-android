package org.rfcx.incidents.widget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.rfcx.incidents.databinding.DialogNumberPickerBinding

class NumberPickerDialog(private val callback: NumberPickerButtonClickListener) : DialogFragment() {

    lateinit var binding: DialogNumberPickerBinding
    private var prefsNumberValue = 0

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        prefsNumberValue = arguments?.getInt(ARG_VALUE) ?: 0
        binding = DialogNumberPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.outsidePicker.setOnClickListener {
            dismissDialog()
        }

        binding.cancelButton.setOnClickListener {
            dismissDialog()
        }

        binding.nextButton.setOnClickListener {
            dismissDialog()
            callback.onNextClicked(binding.numberPicker.value)
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.numberPicker.maxValue = 30
        binding.numberPicker.minValue = 0
        binding.numberPicker.value = prefsNumberValue
    }

    private fun dismissDialog() {
        try {
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
            dismissAllowingStateLoss()
        }
    }

    companion object {
        private const val ARG_VALUE = "ARG_VALUE"

        fun newInstance(number: Int, callback: NumberPickerButtonClickListener) = NumberPickerDialog(callback).apply {
            arguments = Bundle().apply {
                putInt(ARG_VALUE, number)
            }
        }
    }
}

interface NumberPickerButtonClickListener {
    fun onNextClicked(number: Int)
}
