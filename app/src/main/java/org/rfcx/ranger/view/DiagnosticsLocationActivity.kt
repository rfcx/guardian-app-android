package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_dianostics_location.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R

class DiagnosticsLocationActivity : AppCompatActivity() {
	
	companion object {
		fun startIntent(context: Context?) {
			context?.startActivity(Intent(context, DiagnosticsLocationActivity::class.java))
		}
		
		private const val MAP_MODE = 1
		private const val LIST_MODE = 2
	}
	
	private var viewMode: Int = MAP_MODE
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_dianostics_location)
		setupActionBar()
		showDiagnosticMap()
	}
	
	private fun showDiagnosticMap() {
		viewMode = MAP_MODE
		invalidateOptionsMenu()
		supportFragmentManager.beginTransaction()
				.replace(container.id, DiagnosticsMapFragment.newInstance(), null).commit()
	}
	
	private fun showDiagnosticList() {
		viewMode = LIST_MODE
		invalidateOptionsMenu()
		supportFragmentManager.beginTransaction()
				.replace(container.id, DiagnosticsListFragment.newInstance(), null).commit()
	}
	
	
	private fun setupActionBar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			title = getString(R.string.menu_check_in_history)
			setDisplayHomeAsUpEnabled(true)
		}
	}
	
	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		if (BuildConfig.DEBUG) {
			val inflater = menuInflater
			inflater.inflate(R.menu.diagnostic_menu, menu)
			
			val menuMapView = menu?.findItem(R.id.mapView)
			val menuListView = menu?.findItem(R.id.listView)
			
			if (viewMode == MAP_MODE) {
				menuListView?.isVisible = true
				menuMapView?.isVisible = false
			} else {
				menuListView?.isVisible = false
				menuMapView?.isVisible = true
			}
			
		}
		return super.onCreateOptionsMenu(menu)
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
			R.id.mapView -> showDiagnosticMap()
			R.id.listView -> showDiagnosticList()
		}
		return super.onOptionsItemSelected(item)
	}
	
}