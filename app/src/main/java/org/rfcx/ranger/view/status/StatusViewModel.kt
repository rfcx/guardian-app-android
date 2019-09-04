package org.rfcx.ranger.view.status


import android.content.Context
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.*
import androidx.work.WorkInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.service.ImageUploadWorker
import org.rfcx.ranger.service.LocationSyncWorker
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.adapter.StatusAdapter
import java.util.concurrent.TimeUnit

class StatusViewModel(private val context: Context, private val reportDb: ReportDb, private val reportImageDb: ReportImageDb,
                      private val locationDb: LocationDb, private val profileData: ProfileData,
                      private val weeklySummaryData: WeeklySummaryData) : ViewModel() {
	
	private val reportObserve = Observer<List<Report>> {
		reportList = it
		
		
		// If from old version no have report count.
		// So, we have to add the currently report count in db to pref
		val reportCount = weeklySummaryData.getReportSubmitCount()
		if (reportCount < 1)  {
			weeklySummaryData.adJustRportSubmitCount(reportList.size)
		}
		
		updateWeeklyStat()
		combinedReports()
	}
	
	private val reportImageObserve = Observer<List<ReportImage>> { it ->
		reportsImage.clear()
		val group = it.groupBy { it.reportId }
		group.forEach {
			val all = it.value.count()
			val unsentCount = it.value.filter { reportImage ->
				reportImage.syncState != ReportImageDb.SENT
			}.count()
			reportsImage.append(it.key, ImageState(all, unsentCount))
		}
		combinedReports()
	}
	
	// TODO - Improve this {use follow MainActivity}
	private val workInfoObserve = Observer<List<WorkInfo>> {
		val currentWorkStatus = it?.getOrNull(0)
		if (currentWorkStatus != null) {
			when (currentWorkStatus.state) {
				WorkInfo.State.RUNNING -> {
					updateSyncInfo(SyncInfo.Status.UPLOADING)
				}
				WorkInfo.State.SUCCEEDED -> {
					updateSyncInfo(SyncInfo.Status.UPLOADED)
				}
				else -> {
					updateSyncInfo()
				}
			}
		}
	}
	
	private val _locationTracking = MutableLiveData<Boolean>()
	val locationTracking: LiveData<Boolean> = _locationTracking
	
	private val _profile = MutableLiveData<StatusAdapter.ProfileItem>()
	val profile: LiveData<StatusAdapter.ProfileItem> = _profile
	
	private val _stat = MutableLiveData<StatusAdapter.UserStatusItem>()
	val summaryStat: LiveData<StatusAdapter.UserStatusItem> = _stat
	
	private val _reportItems = MutableLiveData<List<StatusAdapter.ReportItem>>()
	val reportItems: LiveData<List<StatusAdapter.ReportItem>> = _reportItems
	
	private val _syncInfo = MutableLiveData<SyncInfo>()
	val syncInfo: LiveData<SyncInfo> = _syncInfo
	
	private val _hasGuardianGroup = MutableLiveData<Boolean>()
	val hasGuardianGroup: LiveData<Boolean> = _hasGuardianGroup
	
	private var reportsImage: SparseArray<ImageState> = SparseArray()
	private var reportList = listOf<Report>()
	
	private lateinit var reportLiveData: LiveData<List<Report>>
	private lateinit var reportImageLiveData: LiveData<List<ReportImage>>
	
	private lateinit var checkinWorkInfoLiveData: LiveData<List<WorkInfo>>
	private lateinit var reportWorkInfoLiveData: LiveData<List<WorkInfo>>
	private var onDutyRealmTimeDisposable: Disposable? = null
	
	init {
		resumed()
		updateProfile()
		updateWeeklyStat()
		fetchReports()
		fetchJobSyncing()
		
		if (profileData.getTracking()) {
			observeRealTimeOnDuty()
		}
	}
	
	private fun updateProfile() {
		_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
				profileData.getSiteName(), profileData.getTracking())
	}
	
	private fun updateWeeklyStat() {
		Log.d("updateWeeklyStat", "updateWeeklyStat")
		_stat.value = StatusAdapter.UserStatusItem(weeklySummaryData.getOnDutyTimeMinute(),
				weeklySummaryData.getReportSubmitCount(), weeklySummaryData.getReviewCount())
	}
	
	private fun fetchReports() {
		reportLiveData = Transformations.map<RealmResults<Report>, List<Report>>(
				reportDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
		reportLiveData.observeForever(reportObserve)
		
		reportImageLiveData = Transformations.map<RealmResults<ReportImage>, List<ReportImage>>(
				reportImageDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
		
		reportImageLiveData.observeForever(reportImageObserve)
	}
	
	private fun fetchJobSyncing() {
		
		reportWorkInfoLiveData = ReportSyncWorker.workInfos()
		reportWorkInfoLiveData.observeForever(workInfoObserve)
		
		checkinWorkInfoLiveData = LocationSyncWorker.workInfos()
		checkinWorkInfoLiveData.observeForever(workInfoObserve)
	}
	
	private fun combinedReports() {
		val newItemsList = arrayListOf<StatusAdapter.ReportItem>()
		
		reportList.forEach {
			val imageState: ImageState = reportsImage.get(it.id, ImageState(0, 0))
			newItemsList.add(StatusAdapter.ReportItem(Realm.getDefaultInstance().copyFromRealm(it), imageState))
		}
		_reportItems.value = newItemsList
	}
	
	private fun observeRealTimeOnDuty() {
		onDutyRealmTimeDisposable = Observable.interval(1, 1, TimeUnit.MINUTES)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					updateWeeklyStat()
				}
		
	}
	
	fun updateTracking() {
		_locationTracking.value = profileData.getTracking()
		
		updateProfile()
		
		// verify on duty
		if (profileData.getTracking()) {
			observeRealTimeOnDuty()
		} else {
			onDutyRealmTimeDisposable?.dispose()
		}
	}
	
	fun resumed() {
		_hasGuardianGroup.value = profileData.hasGuardianGroup()
		
		ImageUploadWorker.enqueue()
		
		if (locationDb.unsentCount() > 0) {
			LocationSyncWorker.enqueue()
		}
		
		if (reportDb.unsentCount() > 0) {
			ReportSyncWorker.enqueue()
		}
	}
	
	private fun updateSyncInfo(syncStatus: SyncInfo.Status? = null) {
		val status = syncStatus
				?: if (context.isNetworkAvailable()) SyncInfo.Status.STARTING else SyncInfo.Status.WAITING_NETWORK
		if (profileData.getLastStatusSyncing() == SyncInfo.Status.UPLOADED.name && status == SyncInfo.Status.UPLOADED) {
			return
		}
		
		// update last status syncing
		profileData.setLastStatusSyncing(status.name)
		
		val locationCount = locationDb.unsentCount()
		val reportCount = reportDb.unsentCount()
		
		_syncInfo.value = SyncInfo(status, reportCount, locationCount)
	}
	
	override fun onCleared() {
		reportImageLiveData.removeObserver(reportImageObserve)
		reportLiveData.removeObserver(reportObserve)
		reportWorkInfoLiveData.removeObserver(workInfoObserve)
		checkinWorkInfoLiveData.removeObserver(workInfoObserve)
		onDutyRealmTimeDisposable?.dispose()
		super.onCleared()
	}
}

