package org.rfcx.ranger.view.base

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.rfcx.ranger.R
import org.rfcx.ranger.util.LocationPermissions
import org.rfcx.ranger.util.LocationTracking
import org.rfcx.ranger.util.isOnAirplaneMode

abstract class BaseActivity : AppCompatActivity() {
	
	private val locationPermissions by lazy { LocationPermissions(this) }
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		locationPermissions.handleRequestResult(requestCode, grantResults)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		locationPermissions.handleActivityResult(requestCode, resultCode)
	}
	
	// TODO find the way to refactor this
	fun disableLocationTracking() {
		Log.d("BaseActivity", "disableLocationTracking")
		LocationTracking.set(this, false)
	}
	
	// TODO find the way to refactor this
	fun enableLocationTracking(onCompletionCallback: (Boolean) -> Unit) {
		Log.d("BaseActivity", "enableLocationTracking")
		if (isOnAirplaneMode()) {
			AlertDialog.Builder(this)
					.setTitle(R.string.in_air_plane_mode)
					.setMessage(R.string.pls_off_air_plane_mode)
					.setPositiveButton(R.string.common_ok, null)
					.show()
			LocationTracking.set(this, false)
			onCompletionCallback.invoke(false)
		} else {
			locationPermissions.check { hasPermission: Boolean ->
				Log.d("BaseActivity", "hasPermission $hasPermission")
				LocationTracking.set(this, hasPermission)
				onCompletionCallback.invoke(hasPermission)
			}
		}
	}
}