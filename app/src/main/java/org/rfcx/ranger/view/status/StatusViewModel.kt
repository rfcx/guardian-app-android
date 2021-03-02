package org.rfcx.ranger.view.status


import android.content.Context
import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.*
import androidx.work.WorkInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.data.remote.site.GetSiteNameUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.site.SiteResponse
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.service.ImageUploadWorker
import org.rfcx.ranger.service.LocationSyncWorker
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.adapter.StatusAdapter
import java.util.concurrent.TimeUnit

class StatusViewModel(private val context: Context, private val reportDb: ReportDb, private val reportImageDb: ReportImageDb,
                      private val locationDb: LocationDb, private val profileData: ProfileData,
                      private val weeklySummaryData: WeeklySummaryData, private val eventDb: EventDb,
                      private val eventsUserCase: GetEventsUseCase, private val getSiteName: GetSiteNameUseCase) : ViewModel() {
	
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
	
	private val eventObserve = Observer<List<Event>> { it ->
		val events = eventDb.getEvents()
		updateRecentAlerts(events)
	}
	
	private val _locationTracking = MutableLiveData<Boolean>()
	val locationTracking: LiveData<Boolean> = _locationTracking
	
	private val _profile = MutableLiveData<StatusAdapter.ProfileItem>()
	val profile: LiveData<StatusAdapter.ProfileItem> = _profile
	
	private val _stat = MutableLiveData<StatusAdapter.UserStatusItem>()
	val summaryStat: LiveData<StatusAdapter.UserStatusItem> = _stat
	
	private val _reportItems = MutableLiveData<List<StatusAdapter.ReportItem>>()
	val reportItems: LiveData<List<StatusAdapter.ReportItem>> = _reportItems
	
	private val _alertItems = MutableLiveData<List<StatusAdapter.AlertItem>?>()
	val alertItems: LiveData<List<StatusAdapter.AlertItem>?> = _alertItems
	
	private val _syncInfo = MutableLiveData<SyncInfo>()
	val syncInfo: LiveData<SyncInfo> = _syncInfo
	
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
		getGuardianGroupName()
		updateWeeklyStat()
		fetchReports()
		fetchJobSyncing()
		fetchEventsCache()
		
		if (profileData.getTracking()) {
			observeRealTimeOnDuty()
		}
	}
	
	private fun updateProfile() {
		var minLat = 180.0
		var maxLat = -180.0
		var minLng = 180.0
		var maxLng = -180.0
		
		val preferences = Preferences.getInstance(context)
		val site = preferences.getString(Preferences.SITE_FULLNAME)
		
		if (site.isNullOrEmpty()) {
			_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
					profileData.getSiteName(), profileData.getTracking())
			
			getSiteName.execute(object : DisposableSingleObserver<List<SiteResponse>>() {
				override fun onSuccess(t: List<SiteResponse>) {
					preferences.putString(Preferences.SITE_FULLNAME, t[0].name)
					preferences.putString(Preferences.SITE_TIMEZONE, t[0].timezone)
					_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
							t[0].name, profileData.getTracking())
					
					if (t[0].bounds != null) {
						preferences.putBoolean(Preferences.HAVE_SITE_BOUNDS, true)
						
						for ((index, value) in t[0].bounds.coordinates.withIndex()) {
							for ((index, value1) in value.withIndex()) {
								value1.map {
									if (minLat > it[1]) {
										minLat = it[1]
									}
									if (maxLat < it[1]) {
										maxLat = it[1]
									}
									if (minLng > it[0]) {
										minLng = it[0]
									}
									if (maxLng < it[0]) {
										maxLng = it[0]
									}
								}
							}
						}
						preferences.putString(Preferences.MIN_LATITUDE, minLat.toString())
						preferences.putString(Preferences.MAX_LATITUDE, maxLat.toString())
						preferences.putString(Preferences.MIN_LONGITUDE, minLng.toString())
						preferences.putString(Preferences.MAX_LONGITUDE, maxLng.toString())
					}
				}
				
				override fun onError(e: Throwable) {
					Log.d("getSiteName", "error $e")
				}
			}, "tembe")
		} else {
			_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
					site, profileData.getTracking())
		}
	}
	
	private fun getGuardianGroupName() {
		val guardianGroupFullName = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME)
		val guardianGroup = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP)
		
		if (guardianGroupFullName.isNullOrEmpty() && !guardianGroup.isNullOrEmpty()) {
			profileData.getGuardianGroup()?.let {
				getSiteName.execute(object : DisposableSingleObserver<List<SiteResponse>>() {
					override fun onSuccess(t: List<SiteResponse>) {
						val preferences = Preferences.getInstance(context)
						preferences.putString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME, t[0].name)
					}
					
					override fun onError(e: Throwable) {}
				}, it.shortname)
			}
		}
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
	
	fun onEventReviewed(newEvent: Event, reviewValue: String) {
		val eventItem = _alertsList.firstOrNull { it.event.id == newEvent.id }
		
		eventItem?.let {
			it.event = newEvent
			it.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> StatusAdapter.AlertItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> StatusAdapter.AlertItem.State.REJECT
				else -> StatusAdapter.AlertItem.State.NONE
			}
			
			_alertsList.replace(eventItem) { it2-> it2.event.id == newEvent.id }
			updateWeeklyStat()
			_alertItems.value = _alertsList
		} ?: run {
			_alertItems.value = _alertsList
		}
	}
	
	private fun combinedReports() {
		val newItemsList = arrayListOf<StatusAdapter.ReportItem>()
		
		reportList.forEach {
			val imageState: ImageState = reportsImage.get(it.id, ImageState(0, 0))
			newItemsList.add(StatusAdapter.ReportItem(it.realm.copyFromRealm(it), imageState))
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
		ImageUploadWorker.enqueue()
		updateWeeklyStat()
		
		if (locationDb.unsentCount() > 0) {
			LocationSyncWorker.enqueue()
		}
		
		if (reportDb.unsentCount() > 0) {
			ReportSyncWorker.enqueue()
		}
		
		if (eventDb.getCount() < 1) {
			loadEvents()
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
	
	private fun loadEvents() {
		// start load
		val group = profileData.getGuardianGroup() ?: return  // has guardian group
		
		_alertItems.value = null
		
		val requestFactory = EventsRequestFactory(listOf(group.shortname), "measured_at", "DESC", 3, 0, group.values)
		eventsUserCase.execute(object : ResponseCallback<Pair<List<Event>, Int>> {
			override fun onSuccess(t: Pair<List<Event>, Int>) {
				updateRecentAlerts(t.first)
			}
			
			override fun onError(e: Throwable) {
				Log.i("StatusViewModel", "load events: ${e.localizedMessage}")
			}
		}, requestFactory)
	}
	
	private fun updateRecentAlerts(events: List<Event>) {
		val newItemsList = arrayListOf<StatusAdapter.AlertItem>()
		if (events.isNotEmpty()) {
			events.take(3).map { event ->
				newItemsList.add(event.toAlertItem())
			}
			_alertsList = newItemsList
		}
		_alertItems.value = newItemsList
	}
	
	private fun Event.toAlertItem(): StatusAdapter.AlertItem {
		val state = eventDb.getEventState(this.id)
		return state?.let {
			val result = when (it) {
				ReviewEventFactory.confirmEvent -> StatusAdapter.AlertItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> StatusAdapter.AlertItem.State.REJECT
				else -> StatusAdapter.AlertItem.State.NONE
			}
			StatusAdapter.AlertItem(this, result)
		} ?: run {
			StatusAdapter.AlertItem(this, StatusAdapter.AlertItem.State.NONE)
		}
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

