package org.rfcx.incidents.view.report.deployment.detail.edit

interface EditDeploymentSiteListener {
    fun startMapPickerPage(latitude: Double, longitude: Double, altitude: Double, streamId: Int)
    fun updateDeploymentDetail(name: String, altitude: Double)
}
