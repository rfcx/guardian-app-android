package org.rfcx.ranger.view.report.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_step_five.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.report.BaseImageFragment

class StepFiveFragment : BaseImageFragment() {
	
	companion object {
		@JvmStatic
		fun newInstance() = StepFiveFragment()
	}
	
	override fun didAddImages(imagePaths: List<String>) {}
	
	override fun didRemoveImage(imagePath: String) {}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_step_five, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupImageRecycler()
	}
	
	private fun setupImageRecycler() {
		attachImageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		reportImageAdapter.setImages(arrayListOf())
	}
}
