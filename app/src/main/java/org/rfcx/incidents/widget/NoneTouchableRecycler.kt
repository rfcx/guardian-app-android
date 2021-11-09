package org.rfcx.incidents.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class NoneTouchableRecycler @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
	
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(e: MotionEvent?): Boolean {
		return false
	}
}
