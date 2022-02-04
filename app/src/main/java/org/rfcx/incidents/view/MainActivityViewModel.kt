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
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData

class MainActivityViewModel(
    private val context: Context,
    private val responseDb: ResponseDb,
    private val projectDb: ProjectDb,
    private val streamDb: StreamDb,
    private val getProjectsUseCase: GetProjectsUseCase,
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

    private fun getProjectById(id: Int): Project? = projectDb.getProjectById(id)

    fun getStreamByName(name: String): Stream? = streamDb.getStreamByName(name)

    fun getProjectsFromLocal(): List<Project> = projectDb.getProjects()

    fun getResponsesFromLocal(): List<Response> = responseDb.getResponses()

    private fun getStreamsByProjectCoreId(projectCodeId: String): List<Stream> =
        streamDb.getStreamsByProjectCoreId(projectCodeId)

    fun getProjectName(id: Int): String = projectDb.getProjectById(id)?.name
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

    fun setProjectSelected(id: Int) {
        val preferences = Preferences.getInstance(context)
        preferences.putInt(Preferences.SELECTED_PROJECT, id)
    }

    fun getStreamIdsInProjectId(): List<String> {
        val preferences = Preferences.getInstance(context)
        val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
        val projectCoreId = getProjectById(projectId)?.serverId
        projectCoreId?.let {
            return getStreamsByProjectCoreId(it).map { s -> s.serverId }
        }
        return listOf()
    }
}
