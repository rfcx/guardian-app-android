package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {

        fun subscribeIfRequired(context: Context, callback: ((Boolean) -> Unit)? = null) {
            val preferenceHelper = Preferences.getInstance(context)
            val hasSubscribed = preferenceHelper.getBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP)
            val group = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
            val shouldReceiveNotifications = preferenceHelper.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)

            Log.d("CloudMessaging", "subscribe: hasSubscribed $hasSubscribed, shouldReceiveNotifications $shouldReceiveNotifications")

            if (!hasSubscribed && shouldReceiveNotifications && group != null) {
                FirebaseMessaging.getInstance().subscribeToTopic(group).addOnCompleteListener {
                    if (it.isSuccessful) {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, true)
                        Log.d("CloudMessaging", "subscribe: success")
                    } else {
                        Crashlytics.logException(Exception("Unable to subscribe to cloud messaging for guardian group: $group"))
                        Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for guardian group")
                    }
                    callback?.invoke(it.isSuccessful)
                }
            } else {
                callback?.invoke(true)
            }
        }

        fun unsubscribe(context: Context) {
            val preferenceHelper = Preferences.getInstance(context)
            val group = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)

            if (group != null) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(group).addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Crashlytics.logException(Exception("Unable to unsubscribe to cloud messaging for guardian group: $group"))
                        Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for guardian group")
                    }
                    else {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, false)
                        Log.d("CloudMessaging", "unsubscribe: success")
                    }
                }
            } else {
                preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, false)
                Log.d("CloudMessaging", "unsubscribe: no group")
            }
        }
    }
}