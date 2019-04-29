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
		var imageUrl: String? = null,
		@Expose(serialize = false)
		var syncState: Int = 0) : RealmObject() {
	
	companion object {
		const val FIELD_REPORT_ID = "reportId"
	}
}