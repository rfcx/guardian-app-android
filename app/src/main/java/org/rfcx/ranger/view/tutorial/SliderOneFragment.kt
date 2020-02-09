package org.rfcx.ranger.view.tutorial


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.rfcx.ranger.R

/**
 * A simple [Fragment] subclass.
 */
class SliderOneFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_slider_one, container, false)
	}
	
	companion object {
		fun newInstance() = SliderOneFragment()
	}
}
