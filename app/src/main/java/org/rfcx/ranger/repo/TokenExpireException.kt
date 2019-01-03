package org.rfcx.ranger.repo

import android.content.Context
import org.rfcx.ranger.util.Preferences

class TokenExpireException(context: Context) : Exception() {
	init {
		// clear login pref
		Preferences.getInstance(context).clear()
	}
	
}


