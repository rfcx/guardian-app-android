package org.rfcx.incidents.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {

    companion object {

        private fun topicForProject(id: String): String = "project_$id"

        fun subscribeIfRequired(projectId: String, callback: ((Boolean) -> Unit)? = null) {
            val topic = topicForProject(projectId)
            Log.d("CloudMessaging", "subscribe: project $topic")

            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("CloudMessaging", "subscribe: success to $topic")
                } else {
                    FirebaseCrashlytics.getInstance()
                        .log("Unable to subscribe to cloud messaging for project: $topic")
                    Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for project")
                }
                callback?.invoke(it.isSuccessful)
            }
        }

        fun unsubscribe(projectId: String, callback: ((Boolean) -> Unit)? = null) {
            val topic = topicForProject(projectId)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener {
                if (!it.isSuccessful) {
                    FirebaseCrashlytics.getInstance()
                        .log("Unable to unsubscribe to cloud messaging for guardian group: $topic")
                    Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for project")
                } else {
                    Log.d("CloudMessaging", "unsubscribe: success to $topic")
                }
                callback?.invoke(it.isSuccessful)
            }
        }
    }
}
