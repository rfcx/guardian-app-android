package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.data.api.site.StreamResponse
import org.rfcx.ranger.data.api.site.toStream
import org.rfcx.ranger.entity.Stream

class StreamDb(private val realm: Realm) {
	fun insertStream(response: StreamResponse) {
		realm.executeTransaction {
			val stream = it.where(Stream::class.java)
					.equalTo(Stream.STREAM_SERVER_ID, response.id)
					.findFirst()
			
			if (stream == null) {
				val streamObj = response.toStream()
				val id = (it.where(Stream::class.java).max(Stream.STREAM_ID)
						?.toInt() ?: 0) + 1
				streamObj.id = id
				it.insert(streamObj)
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
