package org.rfcx.incidents.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.incidents.data.api.site.StreamResponse
import org.rfcx.incidents.data.api.site.toStream
import org.rfcx.incidents.entity.Stream

class StreamDb(private val realm: Realm) {
	fun insertStream(response: StreamResponse) {
		realm.executeTransaction {
			val stream = it.where(Stream::class.java)
					.equalTo(Stream.STREAM_SERVER_ID, response.id)
					.findFirst()
			val streamObj = response.toStream()
			if (stream == null) {
				streamObj.id = (it.where(Stream::class.java).max(Stream.STREAM_ID)
						?.toInt() ?: 0) + 1
			} else {
				streamObj.id = stream.id
			}
			it.insertOrUpdate(streamObj)
		}
	}
	
	fun saveIncidentRef(ref: Int, serverId: String) {
		realm.use {
			it.executeTransaction { realm ->
				val stream = realm.where(Stream::class.java).equalTo(Stream.STREAM_SERVER_ID, serverId).findFirst()
				stream?.incidentRef = ref
			}
		}
	}
	
	fun getAllResultsAsync(): RealmResults<Stream> {
		return realm.where(Stream::class.java).findAllAsync()
	}
	
	fun getStreams(): List<Stream> {
		return realm.where(Stream::class.java).findAll() ?: arrayListOf()
	}
	
	fun getStreamByCoreId(serverId: String): Stream? = realm.where(Stream::class.java).equalTo(Stream.STREAM_SERVER_ID, serverId).findFirst()
	
	fun getStreamByName(name: String): Stream? = realm.where(Stream::class.java).equalTo(Stream.STREAM_NAME, name).findFirst()
}
