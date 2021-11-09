package org.rfcx.incidents.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging

class CloudMessaging {
	
	companion object {
		
		fun setProject(context: Context, project: String) {
			val preferenceHelper = Preferences.getInstance(context)
			preferenceHelper.putString(Preferences.SELECTED_PROJECT_CORE_ID, project)
		}
		
		fun subscribeIfRequired(projectCoreId: String, callback: ((Boolean) -> Unit)? = null) {
			val project = "project_$projectCoreId"
			Log.d("CloudMessaging", "subscribe: project $project")
			
			FirebaseMessaging.getInstance().subscribeToTopic(project).addOnCompleteListener {
				if (it.isSuccessful) {
					Log.d("CloudMessaging", "subscribe: success to $project")
				} else {
					FirebaseCrashlytics.getInstance().log("Unable to subscribe to cloud messaging for project: $project")
					Log.e("CloudMessaging", "Unable to subscribe to cloud messaging for guardian group")
				}
				callback?.invoke(it.isSuccessful)
			}
		}
		
		fun unsubscribe(projectCoreId: String, callback: ((Boolean) -> Unit)? = null) {
			val project = "project_$projectCoreId"
			FirebaseMessaging.getInstance().unsubscribeFromTopic(project).addOnCompleteListener {
				if (!it.isSuccessful) {
					FirebaseCrashlytics.getInstance().log("Unable to unsubscribe to cloud messaging for guardian group: $project")
					Log.e("CloudMessaging", "Unable to unsubscribe to cloud messaging for guardian group")
				} else {
					Log.d("CloudMessaging", "unsubscribe: success to $project")
				}
				callback?.invoke(it.isSuccessful)
			}
		}
	}
}
