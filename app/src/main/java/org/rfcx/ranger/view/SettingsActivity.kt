package org.rfcx.ranger.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.guardian.GuardianGroupsAdapter
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.localdb.SiteGuardianDb
import org.rfcx.ranger.repo.api.GuardianGroupsApi
import org.rfcx.ranger.util.*
import java.util.*
import kotlin.collections.ArrayList

class SettingsActivity : AppCompatActivity() {
	
	private val guardianGroupsAdapter by lazy { GuardianGroupsAdapter(this) }

	private val locationPermissions by lazy { LocationPermissions(this) }
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		
		bindActionbar()
		appVersionTextView.text = getString(R.string.app_version_label, BuildConfig.VERSION_NAME)
		bindLocationSwitch()
		bindGuardianGroupSpinner()
		bindNotificationsSwitch()
	}
	
	override fun onResume() {
		super.onResume()

		updateLocationSwitch()
		reloadGuardianGroups()
		updateSiteName()
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		locationPermissions.handleActivityResult(requestCode, resultCode)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		locationPermissions.handleRequestResult(requestCode, grantResults)
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

	private fun bindLocationSwitch() {
		locationTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				enableLocationTracking()
			} else {
				disableLocationTracking()
			}
		}
	}

	private fun updateLocationSwitch() {
		locationTrackingSwitch.isChecked = LocationTracking.isOn(this)
	}

	private fun disableLocationTracking() {
		LocationTracking.set(this, false)
	}

	private fun enableLocationTracking() {
		locationPermissions.check { hasPermission: Boolean ->
			LocationTracking.set(this, hasPermission)
			updateLocationSwitch()
		}
	}

	private fun reloadGuardianGroups() {
		val database = SiteGuardianDb()
		val lastUpdated = Preferences.getInstance(this).getDate(Preferences.GUARDIAN_GROUPS_LAST_UPDATED)
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
				Preferences.getInstance(this@SettingsActivity).putDate(Preferences.GUARDIAN_GROUPS_LAST_UPDATED, Date())
				populateGuardianGroups(groups)
				guardianGroupSpinner.visibility = View.VISIBLE
				guardianGroupProgress.visibility = View.INVISIBLE
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				Log.d("SettingsActivity", "failed: $message")
				if (lastUpdated != null) {
					populateGuardianGroups(database.guardianGroups())
				} else {
					populateGuardianGroups(ArrayList())
				}
				guardianGroupSpinner.visibility = View.VISIBLE
				guardianGroupProgress.visibility = View.INVISIBLE
			}
		})
	}
	
	private fun populateGuardianGroups(groups: List<GuardianGroup>) {
		guardianGroupsAdapter.setData(groups)
		
		val selectedValue = Preferences.getInstance(this).getString(Preferences.SELECTED_GUARDIAN_GROUP)
		if (selectedValue != null) {
			val selectedIndex = groups.indexOfFirst { it.shortname == selectedValue }
			if (selectedIndex != -1) {
				guardianGroupSpinner.setSelection(selectedIndex)
			}
		}
	}
	
	private fun guardianGroupSelected(group: GuardianGroup) {
		Log.d("SettingsActivity", "selected group ${group.shortname} ${group.name}")
		
		val preferenceHelper = Preferences.getInstance(this)
		val currentGroup = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
		
		if (currentGroup == null || currentGroup != group.shortname) {
			CloudMessaging.unsubscribe(this)
			preferenceHelper.putString(Preferences.SELECTED_GUARDIAN_GROUP, group.shortname)
		}

		updateSiteName()
		CloudMessaging.subscribeIfRequired(this)
	}

	private fun bindNotificationsSwitch() {
		eventNotificationsSwitch.isChecked = Preferences.getInstance(this).getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)
		eventNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
			Preferences.getInstance(this).putBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, isChecked)
			if (isChecked) {
				eventNotificationsSwitch.isEnabled = false
				CloudMessaging.subscribeIfRequired(this, {
					runOnUiThread { eventNotificationsSwitch.isEnabled = true }
				})
			} else {
				CloudMessaging.unsubscribe(this)
			}
		}
	}

	private fun updateSiteName() {
		siteValueTextView.text = getSiteName()
	}
}