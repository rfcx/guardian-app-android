package org.rfcx.incidents.view

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.realm.asLiveData
import org.rfcx.incidents.data.preferences.CredentialKeeper
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsUseCase
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream

class MainActivityViewModel(
    private val preferences: Preferences,
    private val context: Context,
    private val responseDb: ResponseDb,
    private val projectDb: ProjectDb,
    private val streamDb: StreamDb,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsUseCase: GetStreamsUseCase,
    credentialKeeper: CredentialKeeper
) : ViewModel() {

    val isRequireToLogin = MutableLiveData<Boolean>()

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects

    fun getResponses(): LiveData<List<Response>> {
        return Transformations.map(responseDb.getAllResultsAsync().asLiveData()) { it }
    }

    init {
        isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
    }

    fun getProjectsFromLocal(): List<Project> = projectDb.getProjects()

    fun getResponsesFromLocal(): List<Response> = responseDb.getResponses()

    fun getStream(serverId: String): Stream? = streamDb.get(serverId)

    fun getProjectName(id: String): String = projectDb.getProject(id)?.name
        ?: context.getString(R.string.all_projects)

    fun fetchProjects() {
        getProjectsUseCase.execute(
            object : DisposableSingleObserver<List<Project>>() {
                override fun onSuccess(t: List<Project>) {
                    _projects.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _projects.value = Result.Error(e)
                }
            },
            GetProjectsParams()
        )
    }

    fun refreshStreams() {
        val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
        getStreamsUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {}

                override fun onError(e: Throwable) {}
            },
            GetStreamsParams(projectId, true, 0)
        )
    }

    fun setProjectSelected(id: String) {
        val preferences = Preferences.getInstance(context)
        preferences.putString(Preferences.SELECTED_PROJECT, id)
    }

    fun getStreamIdsInProjectId(): List<String> {
        val preferences = Preferences.getInstance(context)
        val projectId = preferences.getString(Preferences.SELECTED_PROJECT, "")
        return streamDb.getByProject(projectId).map { s -> s.id }
    }
}
