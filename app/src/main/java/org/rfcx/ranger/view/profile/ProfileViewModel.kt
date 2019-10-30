package org.rfcx.ranger.view.profile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.site.GetSiteNameUseCase
import org.rfcx.ranger.entity.site.SiteResponse
import org.rfcx.ranger.util.Preferences

class ProfileViewModel(private val context: Context, private val profileData: ProfileData, private val getSiteName: GetSiteNameUseCase) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	
	init {
		getSiteName()
		locationTracking.value = profileData.getTracking()
		notificationReceiving.value = profileData.getReceiveNotification()
		appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
		userName.value = profileData.getUserNickname()
	}
	
	private fun getSiteName() {
		userSite.value = profileData.getSiteName()
		
		getSiteName.execute(object : DisposableSingleObserver<List<SiteResponse>>() {
			override fun onSuccess(t: List<SiteResponse>) {
				userSite.value = t[0].name
			}
			
			override fun onError(e: Throwable) {
				userSite.value = profileData.getSiteName()
			}
		}, profileData.getSiteId())
	}
	
	
	fun onReceiving(enable: Boolean) {
		profileData.updateReceivingNotification(enable)
		notificationReceiving.value = enable
	}
	
	fun onTracingStatusChange() {
		locationTracking.value = profileData.getTracking()
	}
	
	fun updateSiteName() {
		guardianGroup.value = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME)
	}
}