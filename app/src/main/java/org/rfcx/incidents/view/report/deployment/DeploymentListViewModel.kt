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
import org.rfcx.incidents.domain.guardian.deploy.GetDeploymentsUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class DeploymentListViewModel(
    private val getDeploymentsUseCase: GetDeploymentsUseCase
) : ViewModel() {

    private val _deployments: MutableStateFlow<List<Deployment>> = MutableStateFlow(emptyList())
    val deployments = _deployments.asStateFlow()

    init {
        getDeployments()
    }

    private fun getDeployments() {
        viewModelScope.launch(Dispatchers.Main) {
            getDeploymentsUseCase.launch().catch {

            }.collectLatest { result ->
                _deployments.tryEmit(result.map { it.freeze() })
            }
        }
    }
}
