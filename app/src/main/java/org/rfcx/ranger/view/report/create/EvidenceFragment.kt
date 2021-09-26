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
import kotlinx.android.synthetic.main.fragment_evidence.*
import org.rfcx.ranger.R

class EvidenceFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_evidence, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		nextStepButton.setOnClickListener {
			listener.handleCheckClicked(3)
		}
		
		val str = SpannableStringBuilder(getString(R.string.what_evidence))
		str.setSpan(StyleSpan(Typeface.ITALIC), 43, 65, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		whenTextView.text = str
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = EvidenceFragment()
	}
}
