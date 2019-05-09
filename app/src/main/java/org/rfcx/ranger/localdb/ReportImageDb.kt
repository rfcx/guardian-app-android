package org.rfcx.ranger.localdb

import android.util.Log
import io.realm.Realm
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
	
	fun save(report: Report, attachImages: List<String>? = null) {
		val imageCreateAt = DateHelper.parse(report.reportedAt, DateHelper.dateTimeFormatSecond)
		realm.executeTransaction {
			// save attached image to be Report Image
			attachImages?.forEach { attachImage ->
				val imageId = (it.where(ReportImage::class.java).max("id")?.toInt() ?: 0) + 1
				val reportImage = ReportImage(imageId, guid = report.guid, reportId = report.id, imageUrl = attachImage, createAt = imageCreateAt)
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
	
	fun markSent(id: Int) {
		mark(id, SENT)
	}
	
	private fun mark(id: Int, syncState: Int) {
		realm.executeTransaction {
			val report = it.where(ReportImage::class.java).equalTo("id", id).findFirst()
			if (report != null) {
				report.syncState = syncState
			}
		}
	}
	
	fun delete(reportId: Int) {
		val shouldDelete = realm.where(ReportImage::class.java)
				.equalTo("reportId", reportId)
				.findAll()
		Log.d("ReportImageDb", "shouldDelete ${shouldDelete.count()}")
		realm.executeTransaction {
			shouldDelete.deleteAllFromRealm()
		}
	}
	
	fun getAllAsync(): List<Report> {
		return realm.copyFromRealm(realm.where(Report::class.java).findAllAsync())
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
	}
}
