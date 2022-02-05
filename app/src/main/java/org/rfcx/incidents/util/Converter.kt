package org.rfcx.incidents.util

import com.google.gson.JsonObject
import java.util.Calendar
import java.util.Date

fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val checkDate = Calendar.getInstance()
    checkDate.time = this

    return today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR) &&
        today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH) &&
        today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR)
}

fun Map<String, String>.toJsonObject(): JsonObject {
    val jsonObject = JsonObject()
    this.forEach {
        jsonObject.addProperty(it.key, it.value)
    }
    return jsonObject
}
