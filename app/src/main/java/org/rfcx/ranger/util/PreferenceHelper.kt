package org.rfcx.ranger.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
	
	private val PREFS_NAME = "Rfcx.Ranger"
	private var sharedPreferences: SharedPreferences
	
	companion object {
		@Volatile
		private var INSTANCE: PreferenceHelper? = null
		
		fun getInstance(context: Context): PreferenceHelper =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: PreferenceHelper(context).also { INSTANCE = it }
				}
	}
	
	init {
		sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
	}

	fun putBoolean(key: String, value: Boolean) {
		sharedPreferences.edit().putBoolean(key, value).apply()
	}
	
	fun putString(key: String, value: String) {
		sharedPreferences.edit().putString(key, value).apply()
	}
	
	fun putObject(key: String, value: Any) {
		sharedPreferences.edit().putString(key, GsonProvider.getInstance().gson.toJson(value)).apply()
	}

	fun getBoolean(key: String): Boolean {
		return sharedPreferences.getBoolean(key, false)
	}
	
	fun getString(key: String, defValue: String): String {
		return sharedPreferences.getString(key, defValue)
	}

	fun getString(key: String): String? {
		return sharedPreferences.getString(key, null)
	}
	
	fun <E> getObject(key: String, objClass: Class<E>): E? {
		val objectJson = sharedPreferences.getString(key, null) ?: return null
		return GsonProvider.getInstance().gson.fromJson(objectJson, objClass)
	}
	
	fun remove(key: String) {
		sharedPreferences.edit().remove(key).apply()
	}
	
	fun clear() {
		sharedPreferences.edit().clear().apply()
	}

}