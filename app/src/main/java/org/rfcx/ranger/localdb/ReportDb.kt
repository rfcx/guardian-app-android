package org.rfcx.ranger.localdb

import android.util.Log
import io.realm.Realm
import io.realm.RealmList
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage

/**
 * Manage the saving and sending of reports from the local database
 */

class ReportDb(val realm: Realm = Realm.getDefaultInstance()) {

    fun unsentCount(): Long {
        return realm.where(Report::class.java).notEqualTo("syncState", SENT).count()
    }

    fun save(report: Report, attachImages: List<String>? = null) {
        realm.executeTransaction {
            if (report.id == 0) {
                report.id = (it.where(Report::class.java).max("id")?.toInt() ?: 0) + 1
            }
            it.insertOrUpdate(report)

            // save attached image to be Report Image
            attachImages?.forEach { attachImage ->
                val reportImage = ReportImage(reportId = report.id, imageUrl = attachImage)
                Log.d("ReportDb", "save path in store -> reportId: ${reportImage.reportId} path: $attachImage")
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
        mark(id, UNSENT)
    }

    fun markSent(id: Int) {
        mark(id, SENT)
    }

    private fun mark(id: Int, syncState: Int) {
        realm.executeTransaction {
            val report = it.where(Report::class.java).equalTo("id", id).findFirst()
            if (report != null) {
                report.syncState = syncState
            }
        }
    }

    fun getReportImage(reportId: Int): List<ReportImage>? {
        return realm.where(ReportImage::class.java).equalTo(ReportImage.FIELD_REPORT_ID, reportId).findAllAsync()
    }

    // Deletes sent reports, returns a list of files that can also be deleted
    fun deleteSent(): List<String> {
        val reports = realm.where(Report::class.java).equalTo("syncState", SENT).findAll()
        val filenames = reports.mapNotNull { it.audioLocation }
        realm.executeTransaction {
            reports.deleteAllFromRealm()
        }
        return filenames
    }

    companion object {
        private const val UNSENT = 0
        private const val SENDING = 1
        private const val SENT = 2
    }
}
