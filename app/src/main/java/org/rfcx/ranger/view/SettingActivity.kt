package org.rfcx.ranger.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_setting.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.service.LocationTrackerService
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import org.rfcx.ranger.util.isLocationAllow

class SettingActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_setting)
		
		bindActionbar()
		appVersionTextView.text = getString(R.string.app_version_label, BuildConfig.VERSION_NAME)
		bindSwitchLocation()
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun bindActionbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.setting_label)
		}
	}
	
	private fun bindSwitchLocation() {
		val state = PreferenceHelper.getInstance(this)
				.getString(PrefKey.ENABLE_LOCATION_TRACKING, "")
		if (state.isEmpty()) {
			// state never setting before
			if (this.isLocationAllow()) {
				// state on
				PreferenceHelper.getInstance(this)
						.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_ON)
				locationTrackingSwitch.isChecked = true
			} else {
				PreferenceHelper.getInstance(this)
						.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_OFF)
				locationTrackingSwitch.isChecked = false
			}
		} else {
			locationTrackingSwitch.isChecked = state == TRACKING_ON
		}
		
		// action on switch change
		locationTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				
				// start service
				if (isLocationAllow()) {
					startService(Intent(this@SettingActivity, LocationTrackerService::class.java))
					PreferenceHelper.getInstance(this)
							.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_ON)
				} else {
					// request location permission
					locationTrackingSwitch.isChecked = false
					requestPermissions()
				}
			} else {
				PreferenceHelper.getInstance(this)
						.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_OFF)
				
				// stop tracking location service
				stopService(Intent(this@SettingActivity, LocationTrackerService::class.java))
			}
		}
	}
	
	private fun requestPermissions() {
		val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@SettingActivity,
				Manifest.permission.ACCESS_FINE_LOCATION)
		
		if (!shouldProvideRationale) {
			val dialogBuilder: AlertDialog.Builder =
					AlertDialog.Builder(this@SettingActivity).apply {
						setTitle(null)
						setMessage(R.string.location_permission_msg)
						setPositiveButton(R.string.go_to_setting) { _, _ ->
							val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
									Uri.parse("package:$packageName"))
							intent.addCategory(Intent.CATEGORY_DEFAULT)
							intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
							startActivity(intent)
						}
					}
			dialogBuilder.create().show()
			
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
						REQUEST_PERMISSIONS_REQUEST_CODE)
			}
		}
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
	                                        grantResults: IntArray) {
		Log.d("onRequestPermission", "onRequestPermissionsResult: " + requestCode + permissions.toString())
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// start location service
				startService(Intent(this@SettingActivity, LocationTrackerService::class.java))
				PreferenceHelper.getInstance(this)
						.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_ON)
			} else {
				locationTrackingSwitch.isChecked = false
			}
		}
	}
	
	companion object {
		private const val TRACKING_ON = "on"
		const val TRACKING_OFF = "off"
		private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
	}
	
}