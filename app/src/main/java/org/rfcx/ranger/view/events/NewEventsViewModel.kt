package org.rfcx.ranger.view.events

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.Preferences

class NewEventsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb) : ViewModel() {
	
	fun getProjects() {
		getProjects.execute(object : DisposableSingleObserver<List<ProjectResponse>>() {
			override fun onSuccess(t: List<ProjectResponse>) {
				t.map {
					projectDb.insertOrUpdate(it)
				}
			}
			
			override fun onError(e: Throwable) {
				TODO("Not yet implemented")
			}
		}, ProjectsRequestFactory())
	}
	
	fun getProjectsFromLocal(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun getProjectName(): String {
		val preferences = Preferences.getInstance(context)
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		val project = projectDb.getProjectById(projectId)
		return project?.name ?: context.getString(R.string.all_projects)
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
}
