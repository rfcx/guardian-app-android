package org.rfcx.incidents.view.alert

import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.entity.alert.Alert

class AlertDetailViewModel(private val alertDb: AlertDb): ViewModel()  {
	fun getAlert(coreId: String): Alert? = alertDb.getAlert(coreId)
}
