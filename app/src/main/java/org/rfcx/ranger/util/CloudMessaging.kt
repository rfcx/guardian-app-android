package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {
        
        fun setProject(context: Context, project: String) {
            val preferenceHelper = Preferences.getInstance(context)
            preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_PROJECT, false)
            preferenceHelper.putString(Preferences.SELECTED_PROJECT_CORE_ID, project)
        }

        fun subscribeIfRequired(context: Context, callback: ((Boolean) -> Unit)? = null) {
            val preferenceHelper = Preferences.getInstance(context)
            val hasSubscribed = preferenceHelper.getBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_PROJECT)
            val project = "project_" + preferenceHelper.getString(Preferences.SELECTED_PROJECT_CORE_ID)
            val shouldReceiveNotifications = preferenceHelper.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)

            Log.d("CloudMessaging", "subscribe: project $project, hasSubscribed $hasSubscribed, shouldReceiveNotifications $shouldReceiveNotifications")

            if (!hasSubscribed && shouldReceiveNotifications) {
                FirebaseMessaging.getInstance().subscribeToTopic(project).addOnCompleteListener {
                    if (it.isSuccessful) {
                        preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_PROJECT, true)
                        Log.d("CloudMessaging", "subscribe: success to $project")
                    } else {
                        FirebaseCrashlytics.getInstance().log("Unable to subscribe to cloud messaging for project: $project")
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
            val project = "project_" + preferenceHelper.getString(Preferences.SELECTED_PROJECT_CORE_ID)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(project).addOnCompleteListener {
                if (!it.isSuccessful) {
                    FirebaseCrashlytics.getInstance().log("Unable to unsubscribe to cloud messaging for guardian group: $project")
                    Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for guardian group")
                }
                else {
                    preferenceHelper.putBoolean(Preferences.HAS_SUBSCRIBED_TO_SELECTED_PROJECT, false)
                    Log.d("CloudMessaging", "unsubscribe: success to $project")
                }
                callback?.invoke(it.isSuccessful)
            }
        }
    }
}
