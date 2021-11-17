package org.rfcx.incidents.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.report.Report

class WhenView @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
	
	private var image0: ImageView? = null
	private var image1: ImageView? = null
	private var image2: ImageView? = null
	private var image3: ImageView? = null
	
	private var text0: TextView? = null
	private var text1: TextView? = null
	private var text2: TextView? = null
	private var text3: TextView? = null
	
	private var state: Report.AgeEstimate = Report.AgeEstimate.NONE
	
	private var scale = resources.displayMetrics.density
	private val imageInactivePadding: Int = (4 * scale).toInt()
	
	var onWhenViewStateChangedListener: OnWhenViewStateChangedListener? = null
	
	init {
		View.inflate(context, R.layout.widget_when_view, this)
		image0 = findViewById(R.id.lastMonth)
		image1 = findViewById(R.id.lastWeek)
		image2 = findViewById(R.id.today)
		image3 = findViewById(R.id.now)
		
		text0 = findViewById(R.id.lastMonthText)
		text1 = findViewById(R.id.lastWeekText)
		text2 = findViewById(R.id.inADayText)
		text3 = findViewById(R.id.nowTextView)
		
		reset()
		
		image0?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_MONTH)
		}
		
		image1?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_WEEK)
		}
		
		image2?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_24_HR)
		}
		
		image3?.setOnClickListener {
			setState(Report.AgeEstimate.NOW)
		}
		
		text0?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_MONTH)
		}
		
		text1?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_WEEK)
		}
		
		text2?.setOnClickListener {
			setState(Report.AgeEstimate.LAST_24_HR)
		}
		
		text3?.setOnClickListener {
			setState(Report.AgeEstimate.NOW)
		}
		
	}
	
	fun setState(state: Report.AgeEstimate) {
		reset()
		this.state = state
		onWhenViewStateChangedListener?.onStateChange(state)
		when (state) {
			Report.AgeEstimate.NONE -> {
			
			}
			Report.AgeEstimate.LAST_MONTH -> {
				text0?.setTypeface(text0?.typeface, Typeface.BOLD)
				text0?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
				image0?.selected()
			}
			Report.AgeEstimate.LAST_WEEK -> {
				text1?.setTypeface(text0?.typeface, Typeface.BOLD)
				text1?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
				image1?.selected()
			}
			Report.AgeEstimate.LAST_24_HR -> {
				text2?.setTypeface(text0?.typeface, Typeface.BOLD)
				text2?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
				image2?.selected()
			}
			Report.AgeEstimate.NOW -> {
				text3?.setTypeface(text0?.typeface, Typeface.BOLD)
				text3?.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
				image3?.selected()
			}
		}
	}
	
	private fun reset() {
		text0?.typeface = Typeface.DEFAULT
		text1?.typeface = Typeface.DEFAULT
		text2?.typeface = Typeface.DEFAULT
		text3?.typeface = Typeface.DEFAULT
		
		text0?.setTextColor(ContextCompat.getColor(context, R.color.grey_default))
		text1?.setTextColor(ContextCompat.getColor(context, R.color.grey_default))
		text2?.setTextColor(ContextCompat.getColor(context, R.color.grey_default))
		text3?.setTextColor(ContextCompat.getColor(context, R.color.grey_default))
		
		image0?.noneSelect()
		image1?.noneSelect()
		image2?.noneSelect()
		image3?.noneSelect()
	}
	
	private fun ImageView.noneSelect() {
		this.isActivated = false
		this.setPadding(0, imageInactivePadding, 0, imageInactivePadding)
	}
	
	private fun ImageView.selected() {
		this.isActivated = true
		this.setPadding(0, 0, 0, 0)
	}
	
	fun getState(): Report.AgeEstimate = state
	
	fun disable() {
		image0?.setOnClickListener(null)
		image1?.setOnClickListener(null)
		image2?.setOnClickListener(null)
		image3?.setOnClickListener(null)
		text0?.setOnClickListener(null)
		text1?.setOnClickListener(null)
		text2?.setOnClickListener(null)
		text3?.setOnClickListener(null)
	}
	
	interface OnWhenViewStateChangedListener {
		fun onStateChange(state: Report.AgeEstimate)
	}
	
}

