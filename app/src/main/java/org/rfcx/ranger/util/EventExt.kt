package org.rfcx.ranger.util

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.DateHelper.DAY
import java.text.SimpleDateFormat
import java.util.*

fun Event.getIconRes(): Int {

    return when (this.value) {
        Event.chainsaw -> R.drawable.ic_chainsaw
        Event.gunshot -> R.drawable.ic_gun
        Event.vehicle -> R.drawable.ic_truck
        Event.trespasser -> R.drawable.ic_people
        else -> R.drawable.ic_other
    }
}

fun Event.timeAgoDisplay(context: Context): String {
    beginsAt ?: return ""

    val eventDate = DateHelper.getDateTime(beginsAt)
    // timestamp
    val dayAgo = DAY
    val daysAgo = 2 * DAY

    val diff = Date().time - eventDate!!.time
    val diffCalendar = Calendar.getInstance()
    diffCalendar.timeInMillis = diff

    val timeFormat = SimpleDateFormat(DateHelper.timeFormat, Locale.US)

    return if (diff < dayAgo) {
        timeFormat.format(eventDate.time)
    } else if (diff < daysAgo) {
        "${context.getString(R.string.yesterday)} ${timeFormat.format(eventDate.time)}"
    } else {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.US)
        dateFormat.format(diffCalendar.time)
    }
}