package org.rfcx.ranger.util

import io.realm.Realm
import org.rfcx.ranger.entity.report.Report

/**
 * Manage the saving and sending of reports from the local database
 */

class ReportDb(val realm: Realm = Realm.getDefaultInstance()) {

    fun unsentCount(): Long {
        return realm.where(Report::class.java).notEqualTo("syncState", SENT).count()
    }

    fun save(report: Report) {
        realm.use { realm ->
            realm.executeTransaction {
                it.insertOrUpdate(report)
            }
        }
    }

    fun lockUnsent(): List<Report> {
        var lockedReports: List<Report> = listOf()
        realm.use { realm ->
            realm.executeTransaction {
                val unsent = it.where(Report::class.java).equalTo("syncState", UNSENT).findAll()
                unsent.setInt("syncState", SENDING)
                lockedReports = unsent.toList()
            }
        }
        return lockedReports
    }

    fun markUnsent(id: Int) {
        mark(id, UNSENT)
    }

    fun markSent(id: Int) {
        mark(id, SENT)
    }

    private fun mark(id: Int, syncState: Int) {
        realm.use { realm ->
            realm.executeTransaction {
                val report = it.where(Report::class.java).equalTo("id", id).findFirst()
                if (report != null) {
                    report.syncState = syncState
                    it.insertOrUpdate(report)
                }
            }
        }
    }

    // Deletes sent reports, returns a list of files that can also be deleted
    fun deleteSent(): List<String> {
        var audioFiles: List<String> = listOf()
        realm.use { realm ->
            realm.executeTransaction {
                val reports = it.where(Report::class.java).equalTo("syncState", SENT).findAll()
                audioFiles = reports.mapNotNull { it.audioLocation }
                reports.deleteAllFromRealm()
            }
        }
        return audioFiles
    }

    companion object {
        private val UNSENT = 0
        private val SENDING = 1
        private val SENT = 2
    }
}
