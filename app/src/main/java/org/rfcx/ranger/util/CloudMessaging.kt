package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {
        
        fun setGroup(context: Context, group: String) {
            val preferenceHelper = Preferences.getInstance(context)
            preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, false)
            preferenceHelper.putString(Preferences.SELECTED_GUARDIAN_GROUP, group)
        }

        fun subscribeIfRequired(context: Context, callback: ((Boolean) -> Unit)? = null) {
            val preferenceHelper = Preferences.getInstance(context)
            val hasSubscribed = preferenceHelper.getBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP)
            val group = "guardiangroup-" + preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
            val shouldReceiveNotifications = preferenceHelper.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)

            Log.d("CloudMessaging", "subscribe: group $group, hasSubscribed $hasSubscribed, shouldReceiveNotifications $shouldReceiveNotifications")

            if (!hasSubscribed && shouldReceiveNotifications) {
                FirebaseMessaging.getInstance().subscribeToTopic(group).addOnCompleteListener {
                    if (it.isSuccessful) {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, true)
                        Log.d("CloudMessaging", "subscribe: success to $group")
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

        fun unsubscribe(context: Context, callback: ((Boolean) -> Unit)? = null) {
            val preferenceHelper = Preferences.getInstance(context)
            val group = "guardiangroup-" + preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(group).addOnCompleteListener {
                if (!it.isSuccessful) {
                    Crashlytics.logException(Exception("Unable to unsubscribe to cloud messaging for guardian group: $group"))
                    Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for guardian group")
                }
                else {
                    preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_GUARDIAN_GROUP, false)
                    Log.d("CloudMessaging", "unsubscribe: success to $group")
                }
                callback?.invoke(it.isSuccessful)
            }
        }
    }
}