package org.rfcx.ranger.view.tutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_tutorial.*
import org.rfcx.ranger.R
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getImage
import org.rfcx.ranger.view.MainActivityNew

class TutorialActivity : AppCompatActivity() {
	
	private val fragmentList: ArrayList<Fragment> = arrayListOf(
			SliderOneFragment.newInstance(),
			SliderTwoFragment.newInstance(),
			SliderThreeFragment.newInstance(),
			SliderFourFragment.newInstance()
	)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_tutorial)
		
		val preferenceHelper = Preferences.getInstance(this)
		val isFirstTime = preferenceHelper.getBoolean(Preferences.IS_FIRST_TIME)
		
		val viewPager2 = findViewById<ViewPager2>(R.id.viewPager2)
		
		viewPager2.adapter = ViewPagerAdapter(this, fragmentList)
		setUpIndicators()
		setCurrentIndicator(0)
		
		viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				setCurrentIndicator(position)
				if (position == 3) {
					skipTextView.visibility = View.INVISIBLE
					nextTextView.text = this@TutorialActivity.getString(R.string.done)
				} else {
					skipTextView.visibility = View.VISIBLE
					nextTextView.text = this@TutorialActivity.getString(R.string.next)
				}
			}
		})
		
		nextTextView.setOnClickListener {
			if (viewPager2.currentItem + 1 < fragmentList.size) {
				viewPager2.currentItem += 1
			} else {
				if (isFirstTime) {
					preferenceHelper.putBoolean(Preferences.IS_FIRST_TIME, false)
					MainActivityNew.startActivity(this@TutorialActivity, null)
				}
				finish()
			}
		}
		
		skipTextView.setOnClickListener {
			if (isFirstTime) {
				preferenceHelper.putBoolean(Preferences.IS_FIRST_TIME, false)
				MainActivityNew.startActivity(this@TutorialActivity, null)
				
				Toast.makeText(this, R.string.available_in_settings, Toast.LENGTH_SHORT).show()
			}
			finish()
		}
	}
	
	private fun setUpIndicators() {
		val indicators = arrayOfNulls<ImageView>(fragmentList.size)
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
	
	class ViewPagerAdapter(fa: FragmentActivity, private val fragments: ArrayList<Fragment>) : FragmentStateAdapter(fa) {
		override fun getItemCount(): Int = fragments.size
		
		override fun createFragment(position: Int): Fragment {
			return fragments[position]
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

