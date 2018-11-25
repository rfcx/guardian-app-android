package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {

        fun subscribeIfRequired(context: Context) {
            val preferenceHelper = Preferences.getInstance(context)
            val group = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)

            if (!preferenceHelper.getBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP) && group != null) {
                FirebaseMessaging.getInstance().subscribeToTopic(group).addOnCompleteListener {
                    if (it.isSuccessful()) {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, true)
                    } else {
                        Crashlytics.logException(Exception("Unable to subscribe to cloud messaging for guardian group: ${group}"))
                        Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for guardian group")
                    }
                }
            }
        }

        fun unsubscribe(context: Context) {
            val preferenceHelper = Preferences.getInstance(context)
            val group = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)

            if (group != null) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(group).addOnCompleteListener {
                    if (!it.isSuccessful()) {
                        Crashlytics.logException(Exception("Unable to unsubscribe to cloud messaging for default site: ${group}"))
                        Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for default site")
                    }
                    else {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, false)
                    }
                }
            }
        }
    }
}