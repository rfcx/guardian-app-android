package org.rfcx.ranger.view.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.util.isOnAirplaneMode
import org.rfcx.ranger.util.showToast

class GuardianGroupViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb) : ViewModel() {
	
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects
	
	fun getProjectsFromLocal(): List<Project> = projectDb.getProjects()
	
	fun fetchProjects() {
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
	
	fun setProjectsAndSubscribe(project: Project, callback: (Boolean) -> Unit) {
		if (project.serverId == null) return callback(false)
		CloudMessaging.subscribeIfRequired(project.serverId!!) { status -> callback(status) }
		CloudMessaging.setProject(context, project.serverId!!)
	}
	
	fun unsubscribeProject(project: Project, callback: (Boolean) -> Unit) {
		if (project.serverId == null) return callback(false)
		CloudMessaging.unsubscribe(project.serverId!!) { status -> callback(status) }
	}
}
