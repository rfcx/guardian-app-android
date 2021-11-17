package org.rfcx.incidents.repo

import android.content.Context
import org.rfcx.incidents.util.Preferences

class TokenExpireException(context: Context) : Exception() {
	init {
		// clear login pref
		Preferences.getInstance(context).clear()
	}
	
}


