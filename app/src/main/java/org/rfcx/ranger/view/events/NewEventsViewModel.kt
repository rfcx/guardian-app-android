package org.rfcx.ranger.view.events

import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.local.ProjectDb

class NewEventsViewModel(private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb) : ViewModel() {
	
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
}
