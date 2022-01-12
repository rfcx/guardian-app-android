package org.rfcx.incidents.view.profile

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.subscribe.SubscribeUseCase
import org.rfcx.incidents.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.util.*
import java.util.*
import kotlin.random.Random

class ProfileViewModel(private val context: Context, private val profileData: ProfileData, private val subscribeUseCase: SubscribeUseCase, private val unsubscribeUseCase: UnsubscribeUseCase, private val eventDb: EventDb, private val projectDb: ProjectDb) : ViewModel() {
	
	val notificationReceiving = MutableLiveData<Boolean>()
	val notificationReceivingByEmail = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val sendToEmail = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	val formatCoordinates = MutableLiveData<String>()
	val showNotificationByEmail = MutableLiveData<Boolean>()
	val eventSubtitle = MutableLiveData<String>()
	val showSystemOptions = MutableLiveData<Boolean>()
	val preferences = Preferences.getInstance(context)
	
	private val _logoutState = MutableLiveData<Boolean>()
	
	init {
		getSiteName()
		notificationReceiving.value = profileData.getReceiveNotification()
		notificationReceivingByEmail.value = profileData.getReceiveNotificationByEmail()
		appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
		userName.value = profileData.getUserNickname()
		sendToEmail.value = "${context.getString(R.string.sent_to)} ${context.getUserEmail()}"
		formatCoordinates.value = "${context.getCoordinatesFormat()}"
		showNotificationByEmail.value = context.getUserEmail() != ""
		showSystemOptions.value = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
		updateEventSubtitle()
	}
	
	fun resumed() {
		updateEventSubtitle()
		getSiteName()
		formatCoordinates.value = "${context.getCoordinatesFormat()}"
	}
	
	private fun getSiteName() {
		val site = Preferences.getInstance(context).getString(Preferences.SITE_FULLNAME)
		if (site.isNullOrEmpty()) {
			userSite.value = profileData.getSiteName()
		} else {
			userSite.value = site
		}
	}
	
	fun onReceiving(enable: Boolean) {
		profileData.updateReceivingNotification(enable)
		notificationReceiving.value = enable
		// set messaging
//		if (enable) {
//			CloudMessaging.subscribeIfRequired(context)
//		} else {
//			CloudMessaging.unsubscribe(context)
//		}
	}
	
	fun onReceivingByEmail(enable: Boolean) {
		if (profileData.hasGuardianGroup()) {
			// call api
			if (enable) {
				if (!profileData.getReceiveNotificationByEmail()) {
					onSubscribe()
					
					profileData.updateReceivingNotificationByEmail(enable)
					notificationReceivingByEmail.value = enable
				}
			} else {
				onUnsubscribe()
				profileData.updateReceivingNotificationByEmail(enable)
				notificationReceivingByEmail.value = enable
			}
		} else {
			notificationReceivingByEmail.value = false
			Toast.makeText(context, context.getString(R.string.please_set_guardian_group), Toast.LENGTH_SHORT).show()
		}
	}
	
	fun onLogout() {
		_logoutState.value = true
		if (profileData.getReceiveNotificationByEmail()) {
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					_logoutState.value = false
					context.logout()
				}
				
				override fun onError(e: Throwable) {
					_logoutState.value = false
				}
			}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
		} else {
			context.logout()
		}
	}
	
	private fun onSubscribe() {
		subscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
			}
			
			override fun onError(e: Throwable) {
				profileData.updateReceivingNotificationByEmail(false)
				notificationReceivingByEmail.value = false
				Toast.makeText(context, context.getString(R.string.error_common), Toast.LENGTH_SHORT).show()
			}
		}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
	}
	
	private fun onUnsubscribe() {
		unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
			}
			
			override fun onError(e: Throwable) {
				profileData.updateReceivingNotificationByEmail(true)
				notificationReceivingByEmail.value = true
				Toast.makeText(context, context.getString(R.string.error_common), Toast.LENGTH_SHORT).show()
			}
		}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
	}
	
	fun updateSiteName() {
		guardianGroup.value = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME)
	}
	
	private fun updateEventSubtitle() {
		val subscribedProjects = getSubscribedProject()?.map { id -> projectDb.getProjectByCoreId(id)?.name }
				?: listOf()
		var subtitle = if (subscribedProjects.isEmpty()) context.getString(R.string.no_projects_selected) else ""
		subscribedProjects.forEachIndexed { index, name ->
			if (index == 0) {
				subtitle += name
			} else if (index == 1) {
				subtitle += ", $name"
			} else if (index == 2 && index == subscribedProjects.size - 1) {
				subtitle += context.getString(R.string.other_project, 1)
			} else {
				if (index == subscribedProjects.size - 1) {
					subtitle += context.getString(R.string.other_projects, subscribedProjects.size - 2)
				}
			}
		}
		eventSubtitle.value = subtitle
	}
	
	private fun getSubscribedProject(): ArrayList<String>? {
		val preferenceHelper = Preferences.getInstance(context)
		return preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
	}
	
	fun randomGuidOfAlert(): Event? {
		val events = eventDb.getEvents()
		val confirmedEvents = events.filter { it.confirmedCount > 0 && it.rejectedCount == 0 && it.value == "chainsaw" }
		if (confirmedEvents.isNullOrEmpty()) return null
		
		val index = Random.nextInt(confirmedEvents.size)
		return confirmedEvents[index]
	}
	
	companion object {
		private const val DELETING_STATE = "DELETING_STATE"
		private const val DOWNLOAD_STATE = "DOWNLOAD_STATE"
		private const val DOWNLOADED_STATE = "DOWNLOADED_STATE"
		private const val UNAVAILABLE = "UNAVAILABLE"
		const val DOWNLOAD_CANCEL_STATE = "DOWNLOAD_CANCEL_STATE"
		const val DOWNLOADING_STATE = "DOWNLOADING_STATE"
	}
}
