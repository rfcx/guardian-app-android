package org.rfcx.incidents.entity.report

import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class ReportImage(
		@PrimaryKey
		var id: Int = 0,
		@Expose(serialize = false)
		var reportId: Int = 0,
		var reportServerId: String? = null,
		var guid: String? = null,
		@Expose(serialize = false)
		var localPath: String = "",   // path of image on device
		@Expose(serialize = false)
		var createAt: Date = Date(), // 2015-05-13 12:53:55
		var syncState: Int = 0,
		var remotePath: String? = null // image url after synced to server
) : RealmObject() {
	companion object {
		const val TABLE_NAME = "ReportImage"
		const val FIELD_REPORT_ID = "reportId"
		const val FIELD_REPORT_SERVER_ID = "reportServerId"
	}
}
