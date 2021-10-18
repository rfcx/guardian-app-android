package org.rfcx.ranger.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.AlertDb
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.alert.Alert
import org.rfcx.ranger.localdb.StreamDb
import org.rfcx.ranger.util.asLiveData

class GuardianEventDetailViewModel(private val alertDb: AlertDb, private val streamDb: StreamDb) : ViewModel() {
	fun getAlerts(): LiveData<List<Alert>> {
		return Transformations.map(alertDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getStream(serverId: String) : Stream? = streamDb.getStreamByServer(serverId)
}
