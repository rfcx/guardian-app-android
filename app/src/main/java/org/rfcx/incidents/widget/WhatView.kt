package org.rfcx.incidents.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.event.Event

class WhatView @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
	
	private var vehicleButton: ImageButton? = null
	private var peopleButton: ImageButton? = null
	private var chainsawButton: ImageButton? = null
	private var gunButton: ImageButton? = null
	private var pinButton: ImageButton? = null
	
	private var selected: Int = -1
	private var event: String? = null
	
	var onWhatViewChangedListener: OnWhatViewChangedListener? = null
	
	init {
		View.inflate(context, R.layout.widget_what_view, this)
		
		vehicleButton = findViewById(R.id.vehicleButton)
		peopleButton = findViewById(R.id.peopleButton)
		chainsawButton = findViewById(R.id.chainsawButton)
		gunButton = findViewById(R.id.gunButton)
		pinButton = findViewById(R.id.pinButton)
		
		reset()
		
		vehicleButton?.setOnClickListener { setSelected(0) }
		peopleButton?.setOnClickListener { setSelected(1) }
		chainsawButton?.setOnClickListener { setSelected(2) }
		gunButton?.setOnClickListener { setSelected(3) }
		pinButton?.setOnClickListener { setSelected(4) }
	}
	
	fun setSelected(select: Int) {
		reset()
		
		this.selected = select
		
		when (this.selected) {
			0 -> {
				vehicleButton?.selected()
				this.event = Event.vehicle
			}
			1 -> {
				peopleButton?.selected()
				this.event = Event.trespasser
			}
			2 -> {
				chainsawButton?.selected()
				this.event = Event.chainsaw
			}
			3 -> {
				gunButton?.selected()
				this.event = Event.gunshot
			}
			4 -> {
				pinButton?.selected()
				this.event = Event.other
			}
		}
		onWhatViewChangedListener?.onViewChange(this.event)
	}
	
	fun getEventSelected(): String? = this.event
	
	private fun ImageButton.noneSelect() {
		this.setColorFilter(ContextCompat.getColor(context, R.color.button_negative))
	}
	
	private fun ImageButton.selected() {
		this.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
	}
	
	private fun reset() {
		vehicleButton?.noneSelect()
		peopleButton?.noneSelect()
		chainsawButton?.noneSelect()
		gunButton?.noneSelect()
		pinButton?.noneSelect()
	}
	
	interface OnWhatViewChangedListener {
		fun onViewChange(event: String?)
	}
}
