package org.rfcx.incidents.view.report.deployment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.freeze
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.domain.guardian.deploy.DeployDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentDeployParams
import org.rfcx.incidents.domain.guardian.deploy.SaveDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentSaveParams
import org.rfcx.incidents.domain.guardian.deploy.GetDeploymentsUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState

class DeploymentListViewModel(
    private val getDeploymentsUseCase: GetDeploymentsUseCase,
    private val preferences: Preferences,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val deployDeploymentUseCase: DeployDeploymentUseCase
) : ViewModel() {

    private val _deployments: MutableStateFlow<List<Deployment>> = MutableStateFlow(emptyList())
    val deployments = _deployments.asStateFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private var currentFilter = FilterDeployment.ALL
    private var currentAllDeployments = listOf<Deployment>()

    init {
        getDeployments()
        getSelectedProject()
    }

    private fun getDeployments() {
        viewModelScope.launch(Dispatchers.Main) {
            getDeploymentsUseCase.launch().catch {

            }.collectLatest { result ->
                currentAllDeployments = result
                filterWithDeployment(result, currentFilter)
            }
        }
    }

    private fun filterWithDeployment(deployments: List<Deployment>, filter: FilterDeployment = FilterDeployment.ALL) {
        when(filter) {
            FilterDeployment.ALL -> {
                _deployments.tryEmit(deployments.map { it.freeze() })
            }
            FilterDeployment.SYNCED -> {
                _deployments.tryEmit(deployments.map<Deployment, Deployment> { it.freeze() }.filter { it.syncState == SyncState.SENT.value })
            }
            FilterDeployment.UNSYNCED -> {
                _deployments.tryEmit(deployments.map<Deployment, Deployment> { it.freeze() }.filter { it.syncState == SyncState.UNSENT.value })
            }
        }
    }

    private fun getSelectedProject() {
        preferences.getString(Preferences.SELECTED_PROJECT)?.let { projectId ->
            viewModelScope.launch(Dispatchers.Main) {
                getLocalProjectUseCase.launch(GetLocalProjectsParams(projectId)).collectLatest { project ->
                    project?.let {
                        _selectedProject.tryEmit(it.name)
                    }
                }
            }
        }
    }

    fun addFilter(filter: FilterDeployment) {
        currentFilter = filter
        filterWithDeployment(currentAllDeployments, filter)
    }

    fun syncDeployment(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            deployDeploymentUseCase.launch(DeploymentDeployParams(id)).collectLatest { result ->
                when(result) {
                    is Result.Error -> {
                        //show error
                    }
                    Result.Loading -> {
                        //show loading
                    }
                    is Result.Success -> {
                        //show success
                    }
                }
            }
        }
    }

    enum class FilterDeployment{
        ALL, SYNCED, UNSYNCED
    }
}
