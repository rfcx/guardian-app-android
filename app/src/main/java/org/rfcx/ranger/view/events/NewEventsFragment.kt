package org.rfcx.ranger.view.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.ranger.R

class NewEventsFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_new_events, container, false)
	}
	
	companion object {
		const val tag = "NewEventsFragment"
		
		@JvmStatic
		fun newInstance() = NewEventsFragment()
	}
}
