package org.rfcx.ranger.view.status


import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.adapter.StatusAdapter
import java.util.concurrent.TimeUnit

class StatusViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb, private val profileData: ProfileData,
                      private val weeklySummaryData: WeeklySummaryData) : ViewModel() {
	
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
	
	private val _profile = MutableLiveData<StatusAdapter.ProfileItem>()
	val profile: LiveData<StatusAdapter.ProfileItem> = _profile
	
	private val _stat = MutableLiveData<StatusAdapter.UserStatusItem>()
	val summaryStat: LiveData<StatusAdapter.UserStatusItem> = _stat
	
	private val _reportItems = MutableLiveData<List<StatusAdapter.ReportItem>>()
	val reportItems: LiveData<List<StatusAdapter.ReportItem>> = _reportItems
	
	private var reportsImage: SparseArray<ImageState> = SparseArray()
	private var reportList = listOf<Report>()
	
	private lateinit var reportLiveData: LiveData<List<Report>>
	private lateinit var reportImageLiveData: LiveData<List<ReportImage>>
	
	private var onDutyRealmTimeDisposable: Disposable? = null
	
	init {
		_profile.value = StatusAdapter.ProfileItem(profileData.getUserNickname(),
				profileData.getSiteName(), profileData.getTracking())
		updateWeeklyStat()
		fetchReports()
		
		if (profileData.getTracking()) {
			observeRealTimeOnDuty()
		}
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
	
	override fun onCleared() {
		reportImageLiveData.removeObserver(reportImageObserve)
		reportLiveData.removeObserver(reportObserve)
		onDutyRealmTimeDisposable?.dispose()
		super.onCleared()
	}
}

