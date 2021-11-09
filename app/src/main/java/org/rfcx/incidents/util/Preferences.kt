package org.rfcx.incidents.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

class Preferences(context: Context) {
	
	var sharedPreferences: SharedPreferences
	
	companion object {
		@Volatile
		private var INSTANCE: Preferences? = null
		
		fun getInstance(context: Context): Preferences =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: Preferences(context).also { INSTANCE = it }
				}
		
		private const val PREFERENCES_NAME = "Rfcx.Ranger"
		const val PREFIX = "org.rfcx.ranger:"
		
		const val ID_TOKEN = "${PREFIX}ID_TOKEN"
		const val ACCESS_TOKEN = "${PREFIX}ACCESS_TOKEN"
		const val REFRESH_TOKEN = "${PREFIX}REFRESH_TOKEN"
		const val USER_GUID = "${PREFIX}USER_GUID"
		const val EMAIL = "${PREFIX}EMAIL"
		const val NICKNAME = "${PREFIX}NICKNAME"
		const val ROLES = "${PREFIX}ROLES"
		const val ACCESSIBLE_SITES = "${PREFIX}ACCESSIBLE_SITES"
		const val DEFAULT_SITE = "${PREFIX}SITE"
		const val SHOULD_RECEIVE_EVENT_NOTIFICATIONS = "${PREFIX}SHOULD_RECEIVE_EVENT_NOTIFICATIONS"
		const val SELECTED_PROJECT_CORE_ID = "${PREFIX}SELECTED_PROJECT_CORE_ID"
		const val SELECTED_GUARDIAN_GROUP = "${PREFIX}SELECTED_GUARDIAN_GROUP"
		const val SELECTED_GUARDIAN_GROUP_FULLNAME = "${PREFIX}SELECTED_GUARDIAN_GROUP_FULLNAME"
		const val SITE_FULLNAME = "${PREFIX}SITE_FULLNAME"
		const val SITE_TIMEZONE = "${PREFIX}SITE_TIMEZONE"
		const val LOGIN_WITH = "${PREFIX}LOGIN_WITH"
		const val ENABLE_LOCATION_TRACKING = "${PREFIX}ENABLE_LOCATION_TRACKING"
		const val LAST_STATUS_SYNCING = "${PREFIX}LAST_STATUS_SYNCING"
		const val EVENT_ONLINE_TOTAL = "${PREFIX}EVENT_ONLINE_TOTAL"
		const val IMAGE_PROFILE = "${PREFIX}IMAGE_PROFILE"
		const val EMAIL_SUBSCRIBE = "${PREFIX}EMAIL_SUBSCRIBE"
		const val COORDINATES_FORMAT = "${PREFIX}COORDINATES_FORMAT"
		const val OFFLINE_MAP_STATE = "${PREFIX}OFFLINE_MAP_STATE"
		
		/*-- New design --*/
		const val SELECTED_PROJECT = "${PREFIX}SELECTED_PROJECT"
		const val LATEST_GET_LOCATION_TIME = "${PREFIX}LATEST_GET_LOCATION_TIME"
		const val LATEST_CURRENT_LOCATION_TIME = "${PREFIX}LATEST_CURRENT_LOCATION_TIME"
		const val SUBSCRIBED_PROJECTS = "${PREFIX}SUBSCRIBED_PROJECTS"
	}
	
	init {
		sharedPreferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
	}
	
	fun getArrayList(key: String): ArrayList<String>? {
		val gson = Gson()
		val json: String? = sharedPreferences.getString(key, null)
		val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
		return gson.fromJson(json, type)
	}
	
	fun putArrayList(key: String, list: ArrayList<String>) {
		val gson = Gson()
		val json: String = gson.toJson(list)
		sharedPreferences.edit().putString(key, json).apply()
	}
	
	fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
		return sharedPreferences.getBoolean(key, defaultValue)
	}
	
	fun putBoolean(key: String, value: Boolean) {
		sharedPreferences.edit().putBoolean(key, value).apply()
	}
	
	fun getString(key: String, defValue: String): String {
		return sharedPreferences.getString(key, defValue) ?: defValue
	}
	
	fun getString(key: String): String? {
		return sharedPreferences.getString(key, null)
	}
	
	fun putString(key: String, value: String) {
		sharedPreferences.edit().putString(key, value).apply()
	}
	
	fun <E> getObject(key: String, objClass: Class<E>): E? {
		val objectJson = sharedPreferences.getString(key, null) ?: return null
		return GsonProvider.getInstance().gson.fromJson(objectJson, objClass)
	}
	
	fun putObject(key: String, value: Any) {
		sharedPreferences.edit().putString(key, GsonProvider.getInstance().gson.toJson(value)).apply()
	}
	
	fun getDate(key: String): Date? {
		val secondsSinceEpoch = sharedPreferences.getLong(key, 0L)
		if (secondsSinceEpoch == 0L) {
			return null
		}
		return Date(secondsSinceEpoch)
	}
	
	fun putDate(key: String, date: Date) {
		sharedPreferences.edit().putLong(key, date.time).apply()
	}
	
	fun getLong(key: String, defValue: Long): Long {
		return sharedPreferences.getLong(key, defValue)
	}
	
	fun putLong(key: String, long: Long) {
		sharedPreferences.edit().putLong(key, long).apply()
	}
	
	fun putInt(key: String, value: Int) {
		sharedPreferences.edit().putInt(key, value).apply()
	}
	
	fun getInt(key: String, defValue: Int): Int {
		return sharedPreferences.getInt(key, defValue)
	}
	
	fun getStringSet(key: String): Set<String> {
		return sharedPreferences.getStringSet(key, setOf()) ?: setOf()
	}
	
	fun putStringSet(key: String, value: Set<String>) {
		sharedPreferences.edit().putStringSet(key, value).apply()
	}
	
	fun remove(key: String) {
		sharedPreferences.edit().remove(key).apply()
	}
	
	fun clear() {
		sharedPreferences.edit().clear().apply()
	}
	
}