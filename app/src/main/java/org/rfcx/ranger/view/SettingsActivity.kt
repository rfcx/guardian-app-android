package org.rfcx.ranger.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_settings.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.service.LocationTrackerService
import org.rfcx.ranger.adapter.guardian.GuardianGroupsAdapter
import org.rfcx.ranger.repo.api.GuardianGroupsApi
import org.rfcx.ranger.util.*
import java.util.*
import kotlin.collections.ArrayList


class SettingsActivity : AppCompatActivity() {

	val guardianGroupsAdapter by lazy { GuardianGroupsAdapter(this) }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		
		bindActionbar()
		appVersionTextView.text = getString(R.string.app_version_label, BuildConfig.VERSION_NAME)
		bindSwitchLocation()
		bindGuardianGroupSpinner()
	}

	override fun onResume() {
		super.onResume()

		reloadGuardianGroups()
		defaultSiteValueTextView.text = this.getSite()
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
					startService(Intent(this@SettingsActivity, LocationTrackerService::class.java))
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
				stopService(Intent(this@SettingsActivity, LocationTrackerService::class.java))
			}
		}
	}

	private fun bindGuardianGroupSpinner() {
		guardianGroupSpinner.adapter = guardianGroupsAdapter
		guardianGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onNothingSelected(p0: AdapterView<*>?) {}

			override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
				val group = guardianGroupsAdapter.getItem(position)
				guardianGroupSelected(group)
			}
		}
	}
	
	private fun requestPermissions() {
		val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@SettingsActivity,
				Manifest.permission.ACCESS_FINE_LOCATION)
		
		if (!shouldProvideRationale) {
			val dialogBuilder: AlertDialog.Builder =
					AlertDialog.Builder(this@SettingsActivity).apply {
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
				startService(Intent(this@SettingsActivity, LocationTrackerService::class.java))
				PreferenceHelper.getInstance(this)
						.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_ON)
			} else {
				locationTrackingSwitch.isChecked = false
			}
		}
	}

	private fun reloadGuardianGroups() {
		val database = RealmHelper.getInstance()
		val lastUpdated = PreferenceHelper.getInstance(this).getDate(PrefKey.GUARDIAN_GROUPS_LAST_UPDATED)
		val cacheTimeMs = 24L * 3600000L // 24 hours
		if (lastUpdated != null && lastUpdated.after(Date(System.currentTimeMillis() - cacheTimeMs))) {
			Log.d("SettingsActivity", "using cache for guardian groups")
			populateGuardianGroups(database.guardianGroups())
			return
		}

		Log.d("SettingsActivity", "reloading guardian groups")
		guardianGroupSpinner.visibility = View.INVISIBLE
		guardianGroupProgress.visibility = View.VISIBLE

		GuardianGroupsApi().getAll(this, object : GuardianGroupsApi.OnGuardianGroupsCallback {
			override fun onSuccess(groups: List<GuardianGroup>) {
				Log.d("SettingsActivity", "got ${groups.size}")
				database.saveGuardianGroups(groups)
				PreferenceHelper.getInstance(this@SettingsActivity).putDate(PrefKey.GUARDIAN_GROUPS_LAST_UPDATED, Date())
				populateGuardianGroups(groups)
				guardianGroupSpinner.visibility = View.VISIBLE
				guardianGroupProgress.visibility = View.INVISIBLE
			}
			override fun onFailed(t: Throwable?, message: String?) {
				Log.d("SettingsActivity", "failed: ${message}")
				if (lastUpdated != null) {
					populateGuardianGroups(database.guardianGroups())
				}
				else {
					populateGuardianGroups(ArrayList())
				}
				guardianGroupSpinner.visibility = View.VISIBLE
				guardianGroupProgress.visibility = View.INVISIBLE
			}
		})
	}

	private fun populateGuardianGroups(groups: List<GuardianGroup>) {
		guardianGroupsAdapter.setData(groups)

		val selectedValue = PreferenceHelper.getInstance(this).getString(PrefKey.SELECTED_GUARDIAN_GROUP)
		if (selectedValue != null) {
			val selectedIndex = groups.indexOfFirst { it.shortname == selectedValue }
			if (selectedIndex != -1) {
				guardianGroupSpinner.setSelection(selectedIndex)
			}
		}
	}

	private fun guardianGroupSelected(group: GuardianGroup) {
		Log.d("SettingsActivity", "selected group ${group.shortname} ${group.name}")

		val preferenceHelper = PreferenceHelper.getInstance(this)
		val currentGroup = preferenceHelper.getString(PrefKey.SELECTED_GUARDIAN_GROUP)

		if (currentGroup == null || currentGroup != group.shortname) {
			CloudMessaging.unsubscribe(this)
			preferenceHelper.putString(PrefKey.SELECTED_GUARDIAN_GROUP, group.shortname)
		}

		CloudMessaging.subscribeIfRequired(this)
	}

	companion object {
		private const val TRACKING_ON = "on"
		const val TRACKING_OFF = "off"
		private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
	}
	
}