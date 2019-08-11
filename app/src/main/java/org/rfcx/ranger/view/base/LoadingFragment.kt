package org.rfcx.ranger.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.rfcx.ranger.R

class LoadingFragment : DialogFragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_loading, container, false)
	}
	
	fun dismissDialog() {
		try {
			dismiss()
		} catch (e: Exception) {
			e.printStackTrace()
			dismissAllowingStateLoss()
		}
	}
	
	companion object {
		fun newInstance() = LoadingFragment()
	}
}