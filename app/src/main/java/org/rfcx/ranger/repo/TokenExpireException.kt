package org.rfcx.ranger.repo

import android.content.Context
import org.rfcx.ranger.util.PreferenceHelper

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class TokenExpireException(context: Context) : Exception() {
	init {
		// clear login pref
		PreferenceHelper.getInstance(context).clear()
	}
	
}


