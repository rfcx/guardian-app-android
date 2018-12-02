package org.rfcx.ranger.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_location_list.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.location.LocationAdapter

class LocationListActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_location_list)
		
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			title = "Locations"
			setDisplayHomeAsUpEnabled(true)
		}
		setupAdapter()
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun setupAdapter() {
		locationRecycler.apply {
			layoutManager = LinearLayoutManager(this@LocationListActivity)
			setHasFixedSize(true)
			adapter = LocationAdapter()
		}
	}
	
}