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
import org.rfcx.ranger.entity.response.DamageScale

class DamageFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	var selected: Int? = null
	
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
			selected?.let { value ->
				listener.setDamage(value)
				listener.handleCheckClicked(StepCreateReport.ACTION.step)
			}
		}
		largeAreaImageView.setOnClickListener {
			selected = DamageScale.LARGE.value
			setOnSelect(it)
		}
		mediumTreesImageView.setOnClickListener {
			selected = DamageScale.MEDIUM.value
			setOnSelect(it)
		}
		smallNumberImageView.setOnClickListener {
			selected = DamageScale.SMALL.value
			setOnSelect(it)
		}
		noVisibleImageView.setOnClickListener {
			selected = DamageScale.NO_VISIBLE.value
			setOnSelect(it)
		}
	}
	
	private fun setOnSelect(selected: View) {
		nextStepButton.isEnabled = true
		
		if (selected == largeAreaImageView) largeAreaImageView.setBackgroundSelected() else largeAreaImageView.setBackgroundNoSelect()
		if (selected == mediumTreesImageView) mediumTreesImageView.setBackgroundSelected() else mediumTreesImageView.setBackgroundNoSelect()
		if (selected == smallNumberImageView) smallNumberImageView.setBackgroundSelected() else smallNumberImageView.setBackgroundNoSelect()
		if (selected == noVisibleImageView) noVisibleImageView.setBackgroundSelected() else noVisibleImageView.setBackgroundNoSelect()
	}
	
	private fun ImageView.setBackgroundSelected() {
		this.setBackgroundResource(R.drawable.bg_selected)
	}
	
	private fun ImageView.setBackgroundNoSelect() {
		this.setBackgroundResource(R.drawable.bg_circle_white)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = DamageFragment()
	}
}
