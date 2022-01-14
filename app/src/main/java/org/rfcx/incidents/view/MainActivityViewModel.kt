package org.rfcx.incidents.view

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.project.ProjectResponse
import org.rfcx.incidents.data.api.project.ProjectsRequestFactory
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.localdb.ResponseDb
import org.rfcx.incidents.localdb.StreamDb
import org.rfcx.incidents.service.ReviewEventSyncWorker
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData
import org.rfcx.incidents.util.isNetworkAvailable

class MainActivityViewModel(private val context: Context, private val responseDb: ResponseDb,
                            private val projectDb: ProjectDb,
                            private val streamDb: StreamDb,
                            private val getProjects: GetProjectsUseCase,
                            credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects
	
	fun getResponses(): LiveData<List<Response>> {
		return Transformations.map(responseDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	val eventGuIdFromNotification = MutableLiveData<String>()
	
	init {
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		ReviewEventSyncWorker.enqueue()
	}
	
	fun getProjectById(id: Int): Project? = projectDb.getProjectById(id)
	
	fun getStreamByName(name: String): Stream? = streamDb.getStreamByName(name)
	
	fun getProjectsFromLocal(): List<Project> = projectDb.getProjects()
	
	fun getResponsesFromLocal(): List<Response> = responseDb.getResponses()
	
	fun getStreamsByProjectCoreId(projectCodeId: String): List<Stream> = streamDb.getStreamsByProjectCoreId(projectCodeId)
	
	fun getProjectName(id: Int): String = projectDb.getProjectById(id)?.name ?: context.getString(R.string.all_projects)
	
	fun fetchProjects() {
		if (context.isNetworkAvailable()) {
			getProjects.execute(object : DisposableSingleObserver<List<ProjectResponse>>() {
				override fun onSuccess(t: List<ProjectResponse>) {
					t.map {
						projectDb.insertOrUpdate(it)
					}
					_projects.value = Result.Success(listOf())
				}
				
				override fun onError(e: Throwable) {
					_projects.value = Result.Error(e)
				}
			}, ProjectsRequestFactory())
		}
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
	
}
