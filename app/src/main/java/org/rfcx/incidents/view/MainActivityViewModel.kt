package org.rfcx.incidents.view

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.realm.asLiveData
import org.rfcx.incidents.data.preferences.CredentialKeeper
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.CredentialVerifier
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsUseCase
import org.rfcx.incidents.entity.common.Err
import org.rfcx.incidents.entity.common.Ok
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.getUserNickname
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivityViewModel(
    private val preferences: Preferences,
    private val context: Context,
    private val responseDb: ResponseDb,
    private val projectDb: ProjectDb,
    private val streamDb: StreamDb,
    private val trackingDb: TrackingDb,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsUseCase: GetStreamsUseCase
) : ViewModel() {

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects

    private val auth0 by lazy {
        val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
        // auth0.isLoggingEnabled = true
        auth0.isOIDCConformant = true
        auth0
    }

    private val authentication by lazy {
        AuthenticationAPIClient(auth0)
    }

    fun getResponses(): LiveData<List<Response>> {
        return Transformations.map(responseDb.getAllResultsAsync().asLiveData()) { it }
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

    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }

    fun refreshStreams(projectId: String, callback: (List<Stream>?) -> Unit) {
        getStreamsUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {
                    callback.invoke(t)
                }

                override fun onError(e: Throwable) {
                    callback.invoke(null)
                }
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

    suspend fun shouldBackToLogin(): Boolean {
        val preferenceHelper = Preferences.getInstance(context)
        val selectedProject = preferenceHelper.getString(Preferences.SELECTED_PROJECT, "")
        val credentialKeeper = CredentialKeeper(context)

        if (credentialKeeper.hasValidCredentials() && selectedProject != "" && context.getUserNickname()
                .substring(0, 1) != "+" && !credentialKeeper.isTokenExpired()
        ) {
            return false
        }
        return refreshToken()
    }

    private suspend fun refreshToken(): Boolean {
        val credentialKeeper = CredentialKeeper(context)
        val credentialVerifier = CredentialVerifier(context)
        val refreshToken = Preferences.getInstance(context).getString(Preferences.REFRESH_TOKEN)
        val token = Preferences.getInstance(context).getString(Preferences.REFRESH_TOKEN)
        if (refreshToken == null) {
            return true
        }
        if (token == null) {
            return true
        }

        return suspendCoroutine { cont ->
            authentication.renewAuth(refreshToken).start(object : BaseCallback<Credentials, AuthenticationException> {
                override fun onSuccess(credentials: Credentials) {
                    val result = credentialVerifier.verify(credentials)
                    when (result) {
                        is Err -> {
                            cont.resume(true)
                        }
                        is Ok -> {
                            val userAuthResponse = result.value
                            credentialKeeper.save(userAuthResponse)
                            cont.resume(false)
                        }
                    }
                }

                override fun onFailure(error: AuthenticationException) {
                    cont.resume(true)
                }
            })
        }
    }
}
