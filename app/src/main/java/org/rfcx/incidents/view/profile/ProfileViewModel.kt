package org.rfcx.incidents.view.profile

import android.content.Context
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.util.logout

class ProfileViewModel(
    private val context: Context,
    profileData: ProfileData,
    private val projectDb: ProjectDb
) : ViewModel() {

    val appVersion = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val eventSubtitle = MutableLiveData<String>()
    val showSystemOptions = MutableLiveData<Boolean>()
    val preferences = Preferences.getInstance(context)

    private val _logoutState = MutableLiveData<Boolean>()

    init {
        appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
        userName.value = profileData.getUserNickname()
        showSystemOptions.value = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        updateEventSubtitle()
    }

    fun resumed() {
        updateEventSubtitle()
    }

    fun onLogout() {
        _logoutState.value = true
        context.logout()
    }

    private fun updateEventSubtitle() {
        val subscribedProjects = getSubscribedProject()?.map { id -> projectDb.getProject(id)?.name }
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
