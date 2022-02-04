package org.rfcx.incidents.view.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.GetProjectsOptions
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.util.CloudMessaging

class SetProjectsViewModel(
    private val context: Context,
    private val getProjectsUseCase: GetProjectsUseCase
) : ViewModel() {
    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    init {
        fetchProjects()
    }

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
            GetProjectsOptions()
        )
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
