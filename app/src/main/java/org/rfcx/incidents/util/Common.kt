package org.rfcx.incidents.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
    }
}

fun String.isValidEmail() =
    this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun Double.setFormatLabel(): String {
    return if (this >= 1000) "${String.format("%.1f", this / 1000)}km" else "${String.format("%.0f", this)}m"
}

fun ImageView.setDrawableImage(context: Context, id: Int) {
    this.setImageDrawable(ContextCompat.getDrawable(context, id))
}

fun View.hideKeyboard() = this.let {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Context.removeLocationUpdates() {
    val locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    val locationReceiverIntent = Intent(this, LocationChangeReceiver::class.java)
    val locationIntent = PendingIntent.getBroadcast(this, 0, locationReceiverIntent, PendingIntent.FLAG_IMMUTABLE)
    locationProviderClient.removeLocationUpdates(locationIntent)
}

fun Context.startLocationChange() {
    val interval = 3L * 60L * 1000L
    val maxWaitTime = 5L * 60L * 1000L

    val locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    val locationReceiverIntent = Intent(this, LocationChangeReceiver::class.java)
    val locationIntent = PendingIntent.getBroadcast(this, 0, locationReceiverIntent, PendingIntent.FLAG_IMMUTABLE)

    val locationRequest =
        LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(interval)
            .setMaxWaitTime(maxWaitTime)
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    locationProviderClient.requestLocationUpdates(locationRequest, locationIntent)
}

fun setupDisplayTheme() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}

fun Float.setFormatLabel(): String {
    return if (this >= 1000) "${String.format("%.1f", this / 1000)}km" else "${String.format("%.0f", this)}m"
}

private val chars = ('a'..'z') + ('0'..'9')
fun randomStreamId(): String = List(12) { chars.random() }.joinToString("")
