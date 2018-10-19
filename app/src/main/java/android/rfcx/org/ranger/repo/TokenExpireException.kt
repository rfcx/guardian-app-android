package android.rfcx.org.ranger.repo

import android.content.Context
import android.rfcx.org.ranger.util.PreferenceHelper

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class TokenExpireException(context: Context) : Exception() {
	init {
		// clear login pref
		PreferenceHelper.getInstance(context).clear()
	}
	
}


