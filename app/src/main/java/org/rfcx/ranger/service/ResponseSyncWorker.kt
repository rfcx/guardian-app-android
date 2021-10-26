package org.rfcx.ranger.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.companion.service.TrackingSyncWorker
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.AlertDb
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.entity.response.toCreateResponseRequest
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.localdb.TrackingFileDb
import org.rfcx.ranger.util.RealmHelper
import java.io.File


/**
 * Background task for syncing data to the server
 */

class ResponseSyncWorker(private val context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val eventService = ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, context)
		val assetsService = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
		val db = ResponseDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val alertDb = AlertDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val reportImageDb = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val trackingFileDb = TrackingFileDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val responses = db.lockUnsent()
		Log.d(TAG, "doWork: found ${responses.size} unsent")
		
		var someFailed = false
		for (response in responses) {
			val result = eventService.createNewResponse(response.toCreateResponseRequest()).execute()
			if (result.isSuccessful) {
				val incidentRef = result.body()?.incidentRef
				val fullId = result.headers().get("Location")
				val id = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length)
				db.markSent(response.id, id, incidentRef)
				trackingFileDb.updateResponseServerId(response.id, id)
				TrackingSyncWorker.enqueue()
				
				val audioFileOrNull = if (!response.audioLocation.isNullOrEmpty()) createLocalFilePart("file", Uri.parse(response.audioLocation!!), "audio/mpeg") else null
				if (id != null) {
					reportImageDb.saveReportServerIdToImage(id, response.id)
					audioFileOrNull?.let { audioFile -> assetsService.uploadAssets(id, audioFile).execute() }
				}
				alertDb.deleteAlertsByStreamId(response.streamId)
			} else {
				someFailed = true
				db.markUnsent(response.id)
			}
		}
		
		// upload attaches image
		ImageUploadWorker.enqueue()
		
		return if (someFailed) Result.retry() else Result.success()
	}
	
	private fun createLocalFilePart(partName: String, fileUri: Uri, mediaType: String): MultipartBody.Part {
		val file = File(fileUri.path)
		val requestFile = RequestBody.create(mediaType.toMediaTypeOrNull(), file)
		return MultipartBody.Part.createFormData(partName, file.name, requestFile)
	}
	
	companion object {
		private const val TAG = "ResponseSyncWorker"
		private const val UNIQUE_WORK_KEY = "ResponseSyncWorkerUniqueKey"
		
		fun enqueue() {
			val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
			val workRequest = OneTimeWorkRequestBuilder<ResponseSyncWorker>().setConstraints(constraints).build()
			WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}
