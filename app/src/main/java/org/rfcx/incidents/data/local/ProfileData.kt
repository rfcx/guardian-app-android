package org.rfcx.incidents.data.local

import org.rfcx.incidents.util.Preferences
import java.util.*

class ProfileData(private val preferences: Preferences) {
    
    fun getUserNickname(): String {
        val nickname = preferences.getString(Preferences.NICKNAME)
        return if (nickname != null && nickname.isNotEmpty()) nickname.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        } else "Responder"
    }
    
    fun getReceiveNotification(): Boolean {
        return preferences.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)
    }
    
    fun getReceiveNotificationByEmail(): Boolean {
        return preferences.getBoolean(Preferences.EMAIL_SUBSCRIBE, false)
    }
    
    fun updateReceivingNotificationByEmail(received: Boolean) {
        preferences.putBoolean(Preferences.EMAIL_SUBSCRIBE, received)
    }
    
}
