package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {

        fun subscribeIfRequired(context: Context) {
            val preferenceHelper = PreferenceHelper.getInstance(context)
            val site = preferenceHelper.getString(PrefKey.DEFAULT_SITE)
            if (!preferenceHelper.getBoolean(PrefKey.HAS_SUBSCRIBED_TO_DEFAULT_SITE) && site != null) {

                FirebaseMessaging.getInstance().subscribeToTopic(site).addOnCompleteListener {
                    if (it.isSuccessful()) {
                        preferenceHelper.putBoolean(PrefKey.HAS_SUBSCRIBED_TO_DEFAULT_SITE, true)
                    } else {
                        Crashlytics.logException(Exception("Unable to subscribe to cloud messaging for default site: ${site}"))
                        Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for default site")
                    }
                }
            }
        }

        fun unsubscribe(context: Context) {
            val preferenceHelper = PreferenceHelper.getInstance(context)
            val site = preferenceHelper.getString(PrefKey.DEFAULT_SITE)

            if (site != null) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(site).addOnCompleteListener {
                    if (!it.isSuccessful()) {
                        Crashlytics.logException(Exception("Unable to unsubscribe to cloud messaging for default site: ${site}"))
                        Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for default site")
                    }
                }
            }
        }
    }
}