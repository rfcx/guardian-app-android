package org.rfcx.incidents.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.rfcx.incidents.data.remote.common.GsonProvider
import java.lang.reflect.Type
import java.util.Date

class Preferences(context: Context) {

    var sharedPreferences: SharedPreferences

    companion object {
        @Volatile
        private var INSTANCE: Preferences? = null

        fun getInstance(context: Context): Preferences =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Preferences(context).also { INSTANCE = it }
            }

        private const val PREFERENCES_NAME = "default"

        // User
        const val ID_TOKEN = "ID_TOKEN"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val USER_GUID = "USER_GUID"
        const val EMAIL = "EMAIL"
        const val NICKNAME = "NICKNAME"
        const val IMAGE_PROFILE = "IMAGE_PROFILE"

        // UI state
        const val SELECTED_PROJECT = "SELECTED_PROJECT"
        const val OFFLINE_MAP_STATE = "OFFLINE_MAP_STATE"

        // Caching
        const val LATEST_GET_LOCATION_TIME = "LATEST_GET_LOCATION_TIME"
        const val LATEST_CURRENT_LOCATION_TIME = "LATEST_CURRENT_LOCATION_TIME"
        const val SUBSCRIBED_PROJECTS = "SUBSCRIBED_PROJECTS"
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
