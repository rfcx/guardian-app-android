package org.rfcx.ranger.view.events

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
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.view.events.adapter.GuardianModel


class NewEventsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb) : ViewModel() {
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val projects: LiveData<Result<List<Project>>> get() = _projects
	
	val nearbyGuardians = mutableListOf<GuardianModel>()
	val othersGuardians = mutableListOf<GuardianModel>()
	
	var guardians = listOf(
			GuardianModel("Guardian A", 5, 250.1F),
			GuardianModel("Guardian C", 3, 1050.1F),
			GuardianModel("Guardian E", 2, 200.0F),
			GuardianModel("Guardian B", 5, 2200.0F),
			GuardianModel("Guardian G", 5, 2560.9F),
			GuardianModel("Guardian F", 0, 3560.3F),
			GuardianModel("Guardian I", 0, 560.3F),
			GuardianModel("Guardian K", 6, 5560.3F),
			GuardianModel("Guardian H", 4, 8560.3F),
			GuardianModel("Guardian D", 6, 5050.1F))  // TODO:: Delete @tree
	
	init {
		handledGuardians()
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
	
	private fun handledGuardians() {
		val guardianList = guardians.sortedBy { g -> g.distance }
		guardianList.map {
			if (it.distance >= 2000) {
				othersGuardians.add(it)
			} else {
				nearbyGuardians.add(it)
			}
		}
		othersGuardians.sortByDescending { g -> g.numberOfAlerts }
	}
}
