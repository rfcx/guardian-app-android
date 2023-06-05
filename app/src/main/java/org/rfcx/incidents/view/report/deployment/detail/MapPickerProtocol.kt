package org.rfcx.incidents.view.report.deployment.detail

interface MapPickerProtocol {
    fun onSelectedLocation(latitude: Double, longitude: Double, siteId: Int, name: String)
}
