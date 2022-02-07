package org.rfcx.incidents.data.local

import org.rfcx.incidents.data.preferences.Preferences
import java.util.Locale

class ProfileData(private val preferences: Preferences) {

    fun getUserNickname(): String {
        val nickname = preferences.getString(Preferences.NICKNAME)
        return if (nickname != null && nickname.isNotEmpty()) nickname.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        } else "Responder"
    }
}
