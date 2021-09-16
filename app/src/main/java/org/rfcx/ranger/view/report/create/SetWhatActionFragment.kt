package org.rfcx.ranger.view.report.create

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_set_what_action.*
import kotlinx.android.synthetic.main.fragment_set_what_action.nextStepButton
import org.rfcx.ranger.R

class SetWhatActionFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		nextStepButton.setOnClickListener {
			listener.handleCheckClicked(6)
		}
		
		val str = SpannableStringBuilder(getString(R.string.what_action))
		str.setSpan(StyleSpan(Typeface.ITALIC), 26, 47, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		whatActionTextView.text = str
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_set_what_action, container, false)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = SetWhatActionFragment()
	}
}
