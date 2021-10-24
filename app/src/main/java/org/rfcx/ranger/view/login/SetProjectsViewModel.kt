package org.rfcx.ranger.view.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getGuardianGroup

class SetProjectsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb, private val eventDb: EventDb, private val unsubscribeUseCase: UnsubscribeUseCase, private val subscribeUseCase: SubscribeUseCase) : ViewModel() {
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val projects: LiveData<Result<List<Project>>> get() = _projects
	
	init {
		fetchProjects()
	}
	
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
	
	fun getProjectsFromLocal(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun setProjects(project: Project, callback: (Boolean) -> Unit) {
		project.serverId?.let { serverId ->
			CloudMessaging.setProject(context, serverId)
			CloudMessaging.subscribeIfRequired(context) {
				callback(true)
			}
		}
	}
	
	private fun subscribeByEmail(guardianGroup: String) {
		val preference = Preferences.getInstance(context)
		val isSubscribe = preference.getBoolean(Preferences.EMAIL_SUBSCRIBE, false)
		
		if (isSubscribe) {
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					onSubscribe(guardianGroup)
				}
				
				override fun onError(e: Throwable) {
					Toast.makeText(context, context.getString(R.string.error_unsubscribe_by_email,
							context.getGuardianGroup().toString()), Toast.LENGTH_SHORT).show()
				}
			}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
		}
	}
	
	private fun onSubscribe(guardianGroup: String) {
		subscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {}
			override fun onError(e: Throwable) {}
		}, SubscribeRequest(listOf(guardianGroup)))
	}
}
