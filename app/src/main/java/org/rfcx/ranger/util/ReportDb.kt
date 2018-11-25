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
        realm.executeTransaction {
            it.insertOrUpdate(report)
        }
    }

    fun lockUnsent(): List<Report> {
        val unsent = realm.where(Report::class.java).equalTo("syncState", UNSENT).findAll()
        realm.executeTransaction {
            unsent.setInt("syncState", SENDING)
        }
        return unsent.toList()
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
                it.insertOrUpdate(report)
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
