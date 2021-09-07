package org.rfcx.ranger.view.events

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory

class NewEventsViewModel(private val getProjects: GetProjectsUseCase) : ViewModel() {
	
	fun getProjects() {
		getProjects.execute(object : DisposableSingleObserver<List<ProjectResponse>>() {
			override fun onSuccess(t: List<ProjectResponse>) {
				t.map {
					Log.d("onSuccess", "${it.name}")
					Log.d("onSuccess", "${it.permissions}")
				}
			}
			
			override fun onError(e: Throwable) {
				TODO("Not yet implemented")
			}
		}, ProjectsRequestFactory())
	}
}
