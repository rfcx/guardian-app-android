package org.rfcx.ranger.localdb

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.util.DateHelper

/**
 * Manage the saving and sending of reports from the local database
 */

class ReportImageDb(val realm: Realm = Realm.getDefaultInstance()) {
	
	fun unsentCount(): Long {
		return realm.where(ReportImage::class.java).notEqualTo("syncState", SENT).count()
	}
	
	fun save(report: Report, attachImages: List<String>) {
		val imageCreateAt = DateHelper.parse(report.reportedAt, DateHelper.dateTimeFormatSecond)
		realm.executeTransaction {
			// save attached image to be Report Image
			attachImages.forEach { attachImage ->
				val imageId = (it.where(ReportImage::class.java).max("id")?.toInt() ?: 0) + 1
				val reportImage = ReportImage(imageId, guid = report.guid, reportId = report.id, localPath = attachImage, createAt = imageCreateAt)
				it.insertOrUpdate(reportImage)
			}
		}
	}
	
	fun lockUnsent(): List<ReportImage> {
		var unsentCopied: List<ReportImage> = listOf()
		realm.executeTransaction { it ->
			val unsent = it.where(ReportImage::class.java)
					.equalTo("syncState", UNSENT)
					.isNotNull("guid")
					.findAll().createSnapshot()
			unsentCopied = unsent.toList()
			unsent.forEach {
				it.syncState = SENDING
			}
		}
		return unsentCopied
	}
	
	fun unlockSending() {
		realm.executeTransaction { it ->
			val snapshot = it.where(ReportImage::class.java).equalTo("syncState", SENDING).findAll().createSnapshot()
			snapshot.forEach {
				it.syncState = UNSENT
			}
		}
	}
	
	fun markUnsent(id: Int) {
		mark(id = id, syncState = UNSENT)
	}
	
	fun markSent(id: Int, remotePath: String?) {
		realm.executeTransaction {
			val report = it.where(ReportImage::class.java).equalTo("id", id).findFirst()
			if (report != null) {
				report.syncState = SENT
				report.remotePath = remotePath
			}
		}
	}
	
	private fun mark(id: Int, syncState: Int) {
		realm.executeTransaction {
			val report = it.where(ReportImage::class.java).equalTo("id", id).findFirst()
			if (report != null) {
				report.syncState = syncState
			}
		}
	}
	
	fun deleteAll(reportId: Int) {
		val shouldDelete = realm.where(ReportImage::class.java)
				.equalTo("reportId", reportId)
				.findAll()
		Log.d("ReportImageDb", "shouldDelete ${shouldDelete.count()}")
		realm.executeTransaction {
			shouldDelete.deleteAllFromRealm()
		}
	}
	
	
	fun getSync(reportId: Int): List<ReportImage> {
		return realm.copyFromRealm(realm.where(ReportImage::class.java)
				.equalTo("reportId", reportId)
				.findAll())
	}
	
	fun getAllResultsAsync(): RealmResults<ReportImage> {
		return realm.where(ReportImage::class.java)
				.findAllAsync()
	}
	
	fun getByReportIdAsync(reportId: Int): RealmResults<ReportImage> {
		return realm.where(ReportImage::class.java)
				.equalTo("reportId", reportId)
				.findAllAsync()
	}
	
	fun delete(reportImageId: Int) {
		val shouldDelete = realm.where(ReportImage::class.java)
				.equalTo("id", reportImageId)
				.findAll()
		realm.executeTransaction {
			shouldDelete.deleteAllFromRealm()
		}
	}
	
	private fun saveGuIDtoImages(guid: String, reportId: Int) {
		val images = realm.where(ReportImage::class.java).equalTo("reportId", reportId).findAll()
		images?.forEach {
			Log.i("saveGuIDtoImages", it.localPath)
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
	}
}
