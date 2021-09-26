package org.rfcx.ranger.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_damage.*
import org.rfcx.ranger.R

class DamageFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_damage, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		nextStepButton.setOnClickListener {
			listener.handleCheckClicked(5)
		}
		largeAreaImageView.setOnClickListener {
			setOnSelect(it)
		}
		mediumTreesImageView.setOnClickListener {
			setOnSelect(it)
		}
		smallNumberImageView.setOnClickListener {
			setOnSelect(it)
		}
		noVisibleImageView.setOnClickListener {
			setOnSelect(it)
		}
	}
	
	private fun setOnSelect(selected: View) {
		if (selected == largeAreaImageView) largeAreaImageView.setBackgroundSelected() else largeAreaImageView.setBackgroundNoSelect()
		if (selected == mediumTreesImageView) mediumTreesImageView.setBackgroundSelected() else mediumTreesImageView.setBackgroundNoSelect()
		if (selected == smallNumberImageView) smallNumberImageView.setBackgroundSelected() else smallNumberImageView.setBackgroundNoSelect()
		if (selected == noVisibleImageView) noVisibleImageView.setBackgroundSelected() else noVisibleImageView.setBackgroundNoSelect()
	}
	
	private fun ImageView.setBackgroundSelected() {
		this.setBackgroundResource(R.drawable.bg_selected)
	}
	
	private fun ImageView.setBackgroundNoSelect() {
		this.setBackgroundResource(R.drawable.bg_grey_light)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = DamageFragment()
	}
}
