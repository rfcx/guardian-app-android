package org.rfcx.ranger.entity.report

import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ReportImage(
		@PrimaryKey
		var id: Int = 0,
		@Expose(serialize = false)
		var reportId: Int = 0,
		var guid: String? = null,
		@Expose(serialize = false)
		var localPath: String = "",   // path of image on device
		@Expose(serialize = false)
		var createAt: String = "", // 2015-05-13 12:53:55
		var syncState: Int = 0,
		var remotePath: String? = null // image url after synced to server
) : RealmObject() {
	companion object {
		const val FIELD_REPORT_ID = "reportId"
	}
}