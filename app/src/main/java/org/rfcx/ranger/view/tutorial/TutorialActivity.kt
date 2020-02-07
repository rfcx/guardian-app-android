package org.rfcx.ranger.view.tutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_tutorial.*
import kotlinx.android.synthetic.main.fragment_report_view_pager.*
import org.rfcx.ranger.R
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getImage
import org.rfcx.ranger.view.MainActivityNew

class TutorialActivity : AppCompatActivity() {
	
	private val viewPagerAdapter = ViewPagerAdapter(
			listOf(R.drawable.fragment_slider_one,
					R.drawable.fragment_slider_two,
					R.drawable.fragment_slider_three,
					R.drawable.fragment_slider_four)
	)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_tutorial)
		
		val preferenceHelper = Preferences.getInstance(this)
		preferenceHelper.putBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, false)
		
		viewPager2.adapter = viewPagerAdapter
		
		setUpIndicators()
		setCurrentIndicator(0)
		
		viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				setCurrentIndicator(position)
				if (position == 3) {
					nextTextView.text = "Done"
				} else {
					nextTextView.text = "Next"
				}
			}
		})
		
		nextTextView.setOnClickListener {
			if (viewPager2.currentItem + 1 < viewPagerAdapter.itemCount) {
				viewPager2.currentItem += 1
			} else {
				MainActivityNew.startActivity(this@TutorialActivity, null)
				finish()
			}
		}
	}
	
	private fun setUpIndicators() {
		val indicators = arrayOfNulls<ImageView>(viewPagerAdapter.itemCount)
		val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
		layoutParams.setMargins(8, 0, 8, 0)
		for (i in indicators.indices) {
			indicators[i] = ImageView(applicationContext)
			indicators[i].apply {
				this?.setImageDrawable(context.getImage(R.drawable.ic_dot_white))
				this?.layoutParams = layoutParams
			}
			indicatorsContainer.addView(indicators[i])
		}
	}
	
	private fun setCurrentIndicator(index: Int) {
		val childCount = indicatorsContainer.childCount
		for (i in 0 until childCount) {
			val view: View = indicatorsContainer.getChildAt(i)
			if (view is ImageView) {
				if (i == index) {
					view.setImageDrawable(this.getImage(R.drawable.ic_dot_grey))
					
				} else {
					view.setImageDrawable(this.getImage(R.drawable.ic_dot_white))
					
				}
			}
		}
	}
	
	
	companion object {
		fun startActivity(context: Context, eventGuId: String?) {
			val intent = Intent(context, TutorialActivity::class.java)
			if (eventGuId != null)
				intent.putExtra(AlertNotification.ALERT_ID_NOTI_INTENT, eventGuId)
			context.startActivity(intent)
		}
	}
}

interface OnItemClickListener {
	fun onItemClick(eventGuId: String?)
}
