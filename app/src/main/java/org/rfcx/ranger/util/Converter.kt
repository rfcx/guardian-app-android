package org.rfcx.ranger.util

import com.google.gson.JsonObject
import java.util.*

fun Date.isToday(): Boolean {
	val today = Calendar.getInstance()
	val checkDate = Calendar.getInstance()
	checkDate.time = this
	
	return today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
			&& today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH)
			&& today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR)
}

fun Double.limitDecimalPlace(maxDecimalPlaceDigit: Int): String {
	val str = this.toString().split(".")
	return if (str.size > 1 && str[1].length > maxDecimalPlaceDigit) {
		"${str[0]}.${str[1].substring(startIndex = 0, endIndex = maxDecimalPlaceDigit)}"
	} else {
		this.toString()
	}
}

fun Map<String, String>.toJsonObject(): JsonObject {
	val jsonObject = JsonObject()
	this.forEach {
		jsonObject.addProperty(it.key, it.value)
	}
	return jsonObject
}
