package org.rfcx.incidents.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.localdb.StreamDb
import org.rfcx.incidents.util.asLiveData

class GuardianEventDetailViewModel(private val alertDb: AlertDb, private val streamDb: StreamDb) : ViewModel() {
	fun getAlerts(): LiveData<List<Alert>> {
		return Transformations.map(alertDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getStream(serverId: String) : Stream? = streamDb.getStreamByCoreId(serverId)
}
