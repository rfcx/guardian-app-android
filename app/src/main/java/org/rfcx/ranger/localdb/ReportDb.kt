package org.rfcx.ranger.localdb

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.util.DateHelper

/**
 * Manage the saving and sending of reports from the local database
 */

class ReportDb(val realm: Realm = Realm.getDefaultInstance()) {
	fun unsentCount(): Long {
		return realm.where(Report::class.java).notEqualTo("syncState", SENT).count()
	}
	
	fun sentCount(): Long {
		return realm.where(Report::class.java).equalTo("syncState", SENT).count()
	}
	
	fun save(report: Report, attachImages: List<String>? = null) {
		val imageCreateAt = DateHelper.parse(report.reportedAt, DateHelper.dateTimeFormatSecond)
		realm.executeTransaction {
			if (report.id == 0) {
				report.id = (it.where(Report::class.java).max("id")?.toInt() ?: 0) + 1
			}
			it.insertOrUpdate(report)
			
			// save attached image to be Report Image
			attachImages?.forEach { attachImage ->
				val imageId = (it.where(ReportImage::class.java).max("id")?.toInt() ?: 0) + 1
				val reportImage = ReportImage(imageId, reportId = report.id, imageUrl = attachImage, createAt = imageCreateAt)
				it.insertOrUpdate(reportImage)
			}
		}
	}
	
	fun lockUnsent(): List<Report> {
		var unsentCopied: List<Report> = listOf()
		realm.executeTransaction {
			val unsent = it.where(Report::class.java).equalTo("syncState", UNSENT).findAll().createSnapshot()
			unsentCopied = unsent.toList()
			unsent.forEach {
				it.syncState = SENDING
			}
		}
		return unsentCopied
	}
	
	fun unlockSending() {
		realm.executeTransaction {
			val snapshot = it.where(Report::class.java).equalTo("syncState", SENDING).findAll().createSnapshot()
			snapshot.forEach {
				it.syncState = UNSENT
			}
		}
	}
	
	fun markUnsent(id: Int) {
		mark(id = id, syncState = UNSENT)
	}
	
	fun markSent(id: Int, guid: String) {
		mark(id, guid, SENT)
		saveGuIDtoImages(guid, id)
	}
	
	private fun mark(id: Int, guid: String? = null, syncState: Int) {
		realm.executeTransaction {
			val report = it.where(Report::class.java).equalTo("id", id).findFirst()
			if (report != null) {
				report.guid = guid
				report.syncState = syncState
			}
		}
	}
	
	fun getReport(guid: String): Report? {
		return realm.where(Report::class.java).equalTo(Report.FIELD_GUID, guid).findFirst()
	}
	
	fun getReport(id: Int): Report? {
		return realm.where(Report::class.java).equalTo(Report.FIELD_ID, id).findFirst()
	}
	
	fun getReportImages(reportId: Int): List<ReportImage>? {
		return realm.where(ReportImage::class.java).equalTo(ReportImage.FIELD_REPORT_ID, reportId).findAll()
	}
	
	// Deletes sent reports, returns a list of files that can also be deleted
	fun deleteSent(): List<String> {
		val unsentCount = unsentCount()
		var keepSentReportLeft = KEEP_REPORT_COUNT - unsentCount
		if (keepSentReportLeft < 0) keepSentReportLeft = 0
		Log.d("deleteSent", "$keepSentReportLeft")
		
		val sentCount = sentCount()
		val shouldDeleteCount = sentCount - keepSentReportLeft
		Log.i("deleteSent", "$shouldDeleteCount")
		if (shouldDeleteCount <= 0) return emptyList()
		
		val reports = realm.where(Report::class.java)
				.equalTo("syncState", SENT)
				.sort("id", Sort.ASCENDING)
				.limit(shouldDeleteCount)
				.findAll()
		val imageDb = ReportImageDb()
		reports.forEach {
			imageDb.delete(it.id)
		}
		val filenames = reports.mapNotNull { it.audioLocation }
		realm.executeTransaction {
			reports.deleteAllFromRealm()
		}
		return filenames
	}
	
	fun getAllAsync(): List<Report> {
		return realm.copyFromRealm(realm.where(Report::class.java)
				.sort("id", Sort.DESCENDING)
				.findAllAsync())
	}
	
	fun getAllResultsAsync(): RealmResults<Report> {
		return realm.where(Report::class.java)
				.findAllAsync()
	}
	
	private fun saveGuIDtoImages(guid: String, reportId: Int) {
		val images = realm.where(ReportImage::class.java).equalTo("reportId", reportId).findAll()
		images?.forEach {
			Log.i("saveGuIDtoImages", "${it.imageUrl}")
		}
		realm.executeTransaction { transition ->
			images?.forEach {
				val image = it.apply {
					this.guid = guid
				}
				transition.insertOrUpdate(image)
			}
		}
	}
	
	companion object {
		const val UNSENT = 0
		const val SENDING = 1
		const val SENT = 2
		private const val KEEP_REPORT_COUNT = 25
	}
}
