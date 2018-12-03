package org.rfcx.ranger.localdb

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
        realm.executeTransaction {
            if (report.id == 0) {
                report.id = (it.where(Report::class.java).max("id")?.toInt() ?: 0) + 1
            }
            it.insertOrUpdate(report)
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
        private val UNSENT = 0
        private val SENDING = 1
        private val SENT = 2
    }
}
