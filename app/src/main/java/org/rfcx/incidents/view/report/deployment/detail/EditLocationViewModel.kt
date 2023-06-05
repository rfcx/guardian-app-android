package org.rfcx.incidents.view.report.deployment.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.rfcx.companion.entity.Project
import org.rfcx.companion.entity.Stream
import org.rfcx.incidents.view.report.deployment.detail.EditLocationRepository

class EditLocationViewModel(
    application: Application,
    private val editLocationRepository: EditLocationRepository
) : AndroidViewModel(application) {

    fun markDeploymentNeedUpdate(id: Int) {
        return editLocationRepository.markDeploymentNeedUpdate(id)
    }

    fun editStream(
        id: Int,
        locationName: String,
        latitude: Double,
        longitude: Double,
        altitude: Double,
        projectId: Int
    ) {
        return editLocationRepository.editStream(
            id,
            locationName,
            latitude,
            longitude,
            altitude,
            projectId
        )
    }

    fun getStreamById(id: Int): Stream? {
        return editLocationRepository.getStreamById(id)
    }

    fun getProjectById(id: Int): Project? {
        return editLocationRepository.getProjectById(id)
    }
}
