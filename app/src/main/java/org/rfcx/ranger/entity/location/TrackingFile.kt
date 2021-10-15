package org.rfcx.ranger.entity.location

import com.google.gson.annotations.Expose
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class TrackingFile(
		@PrimaryKey
		var id: Int = 0,
		var responseId: Int = 0,
		var responseServerId: String? = null,
		var streamId: Int = 0,
		var streamServerId: String? = null,
		var localPath: String = "",
		var remotePath: String? = null,
		@Expose(serialize = false)
		var syncState: Int = 0
) : RealmModel {
	companion object {
		const val TABLE_NAME = "TrackingFile"
		const val FIELD_ID = "id"
		const val FIELD_RESPONSE_ID = "responseId"
		const val FIELD_RESPONSE_SERVER_ID = "responseServerId"
		const val FIELD_STREAM_ID = "streamId"
		const val FIELD_STREAM_SERVER_ID = "streamServerId"
		const val FIELD_SYNC_STATE = "syncState"
		const val FIELD_LOCAL_PATH = "localPath"
		const val FIELD_REMOTE_PATH = "remotePath"
	}
}
