package org.rfcx.ranger.util

import java.util.*

fun Date.isToday() : Boolean {
	val today = Calendar.getInstance()
	val checkDate = Calendar.getInstance()
	checkDate.time = this
	
	return today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
			&& today.get(Calendar.MONTH) == checkDate.get(Calendar.MONTH)
			&& today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR)
}
