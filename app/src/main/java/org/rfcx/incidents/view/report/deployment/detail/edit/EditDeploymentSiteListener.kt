package org.rfcx.incidents.view.report.deployment.detail.edit

import org.rfcx.incidents.entity.stream.Stream

interface EditDeploymentSiteListener {
    fun startMapPickerPage(site: Stream)
    fun backToEditPage(site: Stream)
    fun finishEdit()
}
