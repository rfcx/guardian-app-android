package org.rfcx.incidents.view.profile

import android.content.Context
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.getUserEmail
import org.rfcx.incidents.util.logout

class ProfileViewModel(
    private val context: Context,
    private val profileData: ProfileData,
    private val projectDb: ProjectDb
) : ViewModel() {

    val notificationReceiving = MutableLiveData<Boolean>()
    val notificationReceivingByEmail = MutableLiveData<Boolean>()
    val appVersion = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val sendToEmail = MutableLiveData<String>()
    val showNotificationByEmail = MutableLiveData<Boolean>()
    val eventSubtitle = MutableLiveData<String>()
    val showSystemOptions = MutableLiveData<Boolean>()
    val preferences = Preferences.getInstance(context)

    private val _logoutState = MutableLiveData<Boolean>()

    init {
        notificationReceiving.value = profileData.getReceiveNotification()
        notificationReceivingByEmail.value = profileData.getReceiveNotificationByEmail()
        appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
        userName.value = profileData.getUserNickname()
        sendToEmail.value = "${context.getString(R.string.sent_to)} ${context.getUserEmail()}"
        showNotificationByEmail.value = context.getUserEmail() != ""
        showSystemOptions.value = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        updateEventSubtitle()
    }

    fun resumed() {
        updateEventSubtitle()
    }

    fun onLogout() {
        _logoutState.value = true
        if (profileData.getReceiveNotificationByEmail()) {
            // TODO Unsubscribe
//            unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
//                override fun onSuccess(t: SubscribeResponse) {
//                    _logoutState.value = false
            context.logout()
//                }
//
//                override fun onError(e: Throwable) {
//                    _logoutState.value = false
//                }
//            }, SubscribeRequest(listOf("XYZ"))) // TODO Replace with unsubscribe from projects
        } else {
            context.logout()
        }
    }

    private fun updateEventSubtitle() {
        val subscribedProjects = getSubscribedProject()?.map { id -> projectDb.getProjectByCoreId(id)?.name }
            ?: listOf()
        var subtitle = if (subscribedProjects.isEmpty()) context.getString(R.string.no_projects_selected) else ""
        subscribedProjects.forEachIndexed { index, name ->
            when {
                index == 0 -> subtitle += name
                index == 1 -> subtitle += ", $name"
                index == 2 && index == subscribedProjects.size - 1 -> subtitle += context.getString(
                    R.string.other_project,
                    1
                )
                index == subscribedProjects.size - 1 -> subtitle += context.getString(
                    R.string.other_projects,
                    subscribedProjects.size - 2
                )
            }
        }
        eventSubtitle.value = subtitle
    }

    private fun getSubscribedProject(): List<String>? {
        val preferenceHelper = Preferences.getInstance(context)
        return preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
    }

    companion object {
        const val DOWNLOAD_CANCEL_STATE = "DOWNLOAD_CANCEL_STATE"
        const val DOWNLOADING_STATE = "DOWNLOADING_STATE"
    }
}
