package org.rfcx.ranger.view

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.report.ReportActivity
import org.rfcx.ranger.widget.BottomNavigationMenuItem

// TODO change class name
class MainActivityNew : BaseActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main_new)
		
		setupBottomMenu()
		
		newReportFabButton.setOnClickListener {
			ReportActivity.startIntent(this, null)
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
			}
			menuMap.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = true
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				supportFragmentManager.beginTransaction()
						.replace(contentContainer.id, MapFragment.newInstance(),
								MapFragment.tag).commit()
			}
			menuAlert.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
			}
			menuProfile.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = true
			}
		}
	}
}