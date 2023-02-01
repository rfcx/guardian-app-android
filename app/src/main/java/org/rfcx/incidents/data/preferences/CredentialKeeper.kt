package org.rfcx.incidents.data.preferences

import android.content.Context
import org.rfcx.incidents.entity.user.UserAuthResponse

/**
 * Saves the credentials and meta data obtained from Auth0
 */

class CredentialKeeper(val context: Context) {

    fun save(user: UserAuthResponse) {
        val preferences = Preferences.getInstance(context)
        // Required
        preferences.putString(Preferences.USER_GUID, user.guid)
        preferences.putString(Preferences.ID_TOKEN, user.idToken)

        // Optional
        if (user.accessToken != null) {
            preferences.putString(Preferences.ACCESS_TOKEN, user.accessToken)
        }
        if (user.refreshToken != null) {
            preferences.putString(Preferences.REFRESH_TOKEN, user.refreshToken)
        }
        if (user.email != null) {
            preferences.putString(Preferences.EMAIL, user.email)
        }
        if (user.nickname != null) {
            preferences.putString(Preferences.NICKNAME, user.nickname)
        }
        if (user.picture != null) {
            preferences.putString(Preferences.IMAGE_PROFILE, user.picture)
        }
        if (user.expiredAt != null) {
            preferences.putLong(Preferences.TOKEN_EXPIRES_AT, user.expiredAt * 1000)
        }
    }

    fun clear() {
        val preferences = Preferences.getInstance(context)
        preferences.clear()
    }

    fun hasValidCredentials(): Boolean {
        val preferences = Preferences.getInstance(context)
        return preferences.getString(Preferences.ID_TOKEN, "").isNotEmpty() && !isTokenExpired()
    }

    private fun isTokenExpired(): Boolean {
        val preferences = Preferences.getInstance(context)
        return System.currentTimeMillis() > preferences.getLong(Preferences.TOKEN_EXPIRES_AT, 0)
    }
}
