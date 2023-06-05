package org.rfcx.incidents.view.report.deployment.detail

import org.rfcx.companion.entity.Project
import org.rfcx.companion.entity.Stream
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream

interface EditLocationActivityListener {
    fun startMapPickerPage(latitude: Double, longitude: Double, altitude: Double, streamId: Int)
    fun updateDeploymentDetail(name: String, altitude: Double)

    fun getStream(id: Int): Stream
    fun getProject(id: Int): Project

    fun startLocationGroupPage()

    fun showAppbar()
    fun hideAppbar()
}
