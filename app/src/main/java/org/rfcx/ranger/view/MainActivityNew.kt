package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alerts.AlertsFragment
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.profile.ProfileFragment
import org.rfcx.ranger.view.report.ReportActivity
import org.rfcx.ranger.view.status.StatusFragment
import org.rfcx.ranger.widget.BottomNavigationMenuItem

// TODO change class name
class MainActivityNew : BaseActivity(), MainActivityEventListener {
	
	private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main_new)
		
		setupBottomMenu()
		
		newReportFabButton.setOnClickListener {
			ReportActivity.startIntent(this, null)
		}
		
		if (savedInstanceState == null) {
			menuStatus.performClick()
		}
		
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
		bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
			override fun onSlide(bottomSheet: View, slideOffset: Float) {
			
			}
			
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					showBottomAppBar()
				}
			}
			
		})
	}
	
	override fun onBackPressed() {
		
		if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
		} else {
			return super.onBackPressed()
		}
	}
	
	private fun setupBottomMenu() {
		menuStatus.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuMap.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuAlert.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuProfile.setOnClickListener {
			onBottomMenuClick(it)
		}
	}
	
	private fun onBottomMenuClick(menu: View) {
		if ((menu as BottomNavigationMenuItem).menuSelected) return
		when (menu.id) {
			menuStatus.id -> {
				menuStatus.menuSelected = true
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				startFragment(StatusFragment.newInstance(), StatusFragment.tag, true)
			}
			menuMap.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = true
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				startFragment(MapFragment.newInstance(), MapFragment.tag, false)
			}
			menuAlert.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				startFragment(AlertsFragment.newInstance(), AlertsFragment.tag, true)
			}
			
			menuProfile.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = true
				startFragment(ProfileFragment.newInstance(), ProfileFragment.tag, true)
			}
		}
	}
	
	override fun showBottomSheet(fragment: Fragment) {
		hidBottomAppBar()
		supportFragmentManager.beginTransaction()
				.replace(bottomSheetContainer.id, fragment, "BottomSheet")
				.commit()
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
	}
	
	override fun hideBottomSheet() {
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
	}
	
	override fun hidBottomAppBar() {
		newReportFabButton.visibility = View.INVISIBLE
		bottomBar.visibility = View.INVISIBLE
	}
	
	override fun showBottomAppBar() {
		bottomBar.visibility = View.VISIBLE
		newReportFabButton.visibility = View.VISIBLE
	}
	
	override fun showAlertPopup(event: Event) {
		AlertBottomDialogFragment.newInstance(event).show(supportFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	private fun startFragment(fragment: Fragment, tag: String = "fragment", showAboveAppbar: Boolean) {
		
		val contentContainerPaddingBottom =
				if (showAboveAppbar) resources.getDimensionPixelSize(R.dimen.bottom_bar_height) else 0
		
		contentContainer.setPadding(0, 0, 0, contentContainerPaddingBottom)
		supportFragmentManager.beginTransaction()
				.replace(contentContainer.id, fragment,
						tag).commit()
	}
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, MainActivityNew::class.java)
			context.startActivity(intent)
		}
	}
}

interface MainActivityEventListener {
	fun showBottomSheet(fragment: Fragment)
	fun hideBottomSheet()
	fun hidBottomAppBar()
	fun showBottomAppBar()
	fun showAlertPopup(event: Event)
}