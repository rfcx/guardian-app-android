package org.rfcx.incidents.view.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.project.ProjectResponse
import org.rfcx.incidents.data.api.project.ProjectsRequestFactory
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.util.CloudMessaging

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