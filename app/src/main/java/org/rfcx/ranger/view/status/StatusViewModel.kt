package org.rfcx.ranger.view.status


import android.content.Context
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
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
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
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.adapter.StatusAdapter
import java.util.concurrent.TimeUnit

class StatusViewModel(private val context: Context, private val reportDb: ReportDb, private val reportImageDb: ReportImageDb,
                      private val locationDb: LocationDb, private val profileData: ProfileData,
                      private val weeklySummaryData: WeeklySummaryData, private val eventDb: EventDb) : ViewModel() {
	
	private val reportObserve = Observer<List<Report>> {
		reportList = it
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
	
	private val eventObserve = Observer<List<Event>> { events ->
		if (events.isNotEmpty()) {
			val newItemsList = arrayListOf<StatusAdapter.AlertItem>()
			for (i in 0..2) {
				val state = eventDb.getEventState(events[i].event_guid)
				state?.let {
					val result = when (it) {
						ReviewEventFactory.confirmEvent -> StatusAdapter.AlertItem.State.CONFIRM
						ReviewEventFactory.rejectEvent -> StatusAdapter.AlertItem.State.REJECT
						else -> StatusAdapter.AlertItem.State.NONE
					}
					newItemsList.add(StatusAdapter.AlertItem(events[i], result))
				} ?: run {
					newItemsList.add(StatusAdapter.AlertItem(events[i], StatusAdapter.AlertItem.State.NONE))
				}
			}
			_alertsList = newItemsList
			_alertItems.value = newItemsList
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
	
	private val _alertItems = MutableLiveData<List<StatusAdapter.AlertItem>>()
	val alertItems: LiveData<List<StatusAdapter.AlertItem>> = _alertItems
	
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
	
	private lateinit var eventsLiveData: LiveData<List<Event>>
	private var _alertsList: List<StatusAdapter.AlertItem> = listOf()
	
	init {
		updateProfile()
		updateWeeklyStat()
		fetchReports()
		fetchJobSyncing()
		fetchEventsCache()
		
		if (profileData.getTracking()) {
			observeRealTimeOnDuty()
		}
	}
	
	private fun updateProfile() {
		_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
				profileData.getSiteName(), profileData.getTracking())
	}
	
	private fun updateWeeklyStat() {
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
	
	private fun fetchEventsCache() {
		// observe events
		eventsLiveData = Transformations.map<RealmResults<Event>, List<Event>>(
				eventDb.getAllResultsAsync().asLiveData()
		) {
			it
		}
		eventsLiveData.observeForever(eventObserve)
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val eventItem = _alertsList.firstOrNull { it.alert.event_guid == eventGuid }
		if (eventItem != null) {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> StatusAdapter.AlertItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> StatusAdapter.AlertItem.State.REJECT
				else -> StatusAdapter.AlertItem.State.NONE
			}
			_alertsList.replace(eventItem) { it.alert.event_guid == eventGuid }
		}
		_alertItems.value = _alertsList
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
		eventsLiveData.removeObserver(eventObserve)
		onDutyRealmTimeDisposable?.dispose()
		super.onCleared()
	}
}

