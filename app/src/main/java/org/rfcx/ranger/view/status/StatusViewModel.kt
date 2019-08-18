package org.rfcx.ranger.view.status


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.realm.RealmResults
import org.rfcx.ranger.adapter.entity.TitleItem
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.view.map.ImageState
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class StatusViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb, private val profileData: ProfileData,
                      private val weeklySummaryData: WeeklySummaryData) : ViewModel() {
	private val _items = MutableLiveData<List<StatusAdapter.StatusItemBase>>()
	val items: LiveData<List<StatusAdapter.StatusItemBase>> = _items
	
	private var _reports: List<Report>? = null // TODO: onChange
	
	val compositeDisposable = CompositeDisposable()
	
	init {
		//TBD - demo ui
		updateFeed()
	}
	
	private fun onDataChange(): Function3<StatusAdapter.ProfileItem, StatusAdapter.UserStatusItem,
			List<StatusAdapter.ReportItem>, List<StatusAdapter.StatusItemBase>> {
		return Function3 { profile, userStatus, reports ->
			val data = arrayListOf<StatusAdapter.StatusItemBase>()
			data.add(profile)
			
			data.add(TitleItem("Your Status")) //TODO: get from strings
			data.add(userStatus)
			
			data.add(TitleItem("Report History"))//TODO: get from strings
			reports.forEach { data.add(it) }
			
			data
		}
	}
	
	private fun getProfile(): Observable<StatusAdapter.ProfileItem> {
		return Observable.just(StatusAdapter.ProfileItem(profileData.getUserNickname(),
				profileData.getSiteName(), profileData.getTracking()))
	}
	
	private fun getUserStatus(): Observable<StatusAdapter.UserStatusItem> {
		return Observable.just(StatusAdapter.UserStatusItem(weeklySummaryData.getOnDutyTimeMinute(),
				weeklySummaryData.getReportSubmitCount(), weeklySummaryData.getReviewCount()))
	}
	
	private fun getReports(): Observable<List<StatusAdapter.ReportItem>> {
		return Observable.just(Data.getSampleReports())
	}
	
	private fun updateFeed() {
		val observer = Observable
				.zip(getProfile().subscribeOn(Schedulers.newThread()),
						getUserStatus().subscribeOn(Schedulers.newThread()),
						getReports().subscribeOn(Schedulers.newThread()),
						onDataChange())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeWith(StatusItemsObserver())
		compositeDisposable.add(observer)
	}
	
	fun onTracking(enable: Boolean) {
		profileData.updateTracking(enable)
		updateFeed()
	}
	
	private inner class StatusItemsObserver : DisposableObserver<List<StatusAdapter.StatusItemBase>>() {
		override fun onComplete() {
		}
		
		override fun onError(e: Throwable) {
		}
		
		override fun onNext(t: List<StatusAdapter.StatusItemBase>) {
			_items.value = t
		}
	}
}

// TBD - demo ui
object Data {
	fun getSampleReports(): List<StatusAdapter.ReportItem> {
		val report1 = Report(id = 0, guid = "xxxx", value = "vehicle", syncState = 0, latitude = 34.595, longitude = 213.508, reportedAt = "2019-08-08 06:50:00")
		val report2 = Report(id = 1, guid = "yyyyy", value = "vehicle", syncState = 0, latitude = 33.595, longitude = 213.508, reportedAt = "2019-08-08 10:00:30")
		val report3 = Report(id = 2, guid = "zzzzzz", value = "trespasser", syncState = 2, latitude = 36.595, longitude = 213.508, reportedAt = "2019-08-08 14:00:30")
		val reportItems = arrayListOf<StatusAdapter.ReportItem>()
		reportItems.add(StatusAdapter.ReportItem(report1, ImageState(2, 0)))
		reportItems.add(StatusAdapter.ReportItem(report2, ImageState(0, 0)))
		reportItems.add(StatusAdapter.ReportItem(report3, ImageState(3, 1)))
		return reportItems
		
	}
}