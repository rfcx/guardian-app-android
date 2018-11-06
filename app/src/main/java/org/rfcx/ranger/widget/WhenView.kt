package org.rfcx.ranger.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.rfcx.ranger.R

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
	
	private var state: State = State.NONE
	
	private var scale = resources.displayMetrics.density
	private val imageInactivePadding: Int = (4 * scale).toInt()
	
	var onWhenViewStatChangedListener: OnWhenViewStatChangedListener? = null
	
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
			setState(State.LAST_MONTH)
		}
		
		image1?.setOnClickListener {
			setState(State.LAST_WEEK)
		}
		
		image2?.setOnClickListener {
			setState(State.LAST_24_HR)
		}
		
		image3?.setOnClickListener {
			setState(State.NOW)
		}
		
		text0?.setOnClickListener {
			setState(State.LAST_MONTH)
		}
		
		text1?.setOnClickListener {
			setState(State.LAST_WEEK)
		}
		
		text2?.setOnClickListener {
			setState(State.LAST_24_HR)
		}
		
		text3?.setOnClickListener {
			setState(State.NOW)
		}
		
	}
	
	private fun setState(state: State) {
		reset()
		this.state = state
		onWhenViewStatChangedListener?.onStateChange(state)
		when (state) {
			State.NONE -> {
			
			}
			State.LAST_MONTH -> {
				text0?.setTypeface(text0?.typeface, Typeface.BOLD)
				image0?.selected()
			}
			State.LAST_WEEK -> {
				text1?.setTypeface(text0?.typeface, Typeface.BOLD)
				image1?.selected()
			}
			State.LAST_24_HR -> {
				text2?.setTypeface(text0?.typeface, Typeface.BOLD)
				image2?.selected()
			}
			State.NOW -> {
				text3?.setTypeface(text0?.typeface, Typeface.BOLD)
				image3?.selected()
			}
		}
	}
	
	private fun reset() {
		text0?.typeface = Typeface.DEFAULT
		text1?.typeface = Typeface.DEFAULT
		text2?.typeface = Typeface.DEFAULT
		text3?.typeface = Typeface.DEFAULT
		
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
	
	fun getState(): State = state
	
	enum class State {
		NONE, LAST_MONTH, LAST_WEEK, LAST_24_HR, NOW
	}
	
	interface OnWhenViewStatChangedListener {
		fun onStateChange(state: State)
	}
	
}

