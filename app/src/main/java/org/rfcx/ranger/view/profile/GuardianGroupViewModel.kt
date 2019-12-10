package org.rfcx.ranger.view.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.util.getResultError

class GuardianGroupViewModel(private val getGuardianGroups: GetGuardianGroups,
                             private val eventDb: EventDb) : ViewModel() {
	
	private val _items = MutableLiveData<Result<List<GuardianGroup>>>()
	val items: LiveData<Result<List<GuardianGroup>>> get() = _items
	
	init {
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		_items.value = Result.Loading
		getGuardianGroups.execute(object : ResponseCallback<List<GuardianGroup>> {
			override fun onSuccess(t: List<GuardianGroup>) {
				_items.value = Result.Success(t)
			}
			
			override fun onError(e: Throwable) {
				_items.value = e.getResultError()
			}
			
		}, GuardianGroupFactory())
	}
	
	/**
	 * remove all events when select guardian group
	 */
	fun removeAllEvent() {
		eventDb.deleteAllEvents()
	}
}

class GetGuardianGroupDisposable(
		private val liveData: MutableLiveData<Result<List<GuardianGroup>>>)
	: BaseDisposableSingle<List<GuardianGroup>>() {
	
	override fun onError(e: Throwable, error: Result<List<GuardianGroup>>) {
		liveData.value = error
	}
	
	override fun onSuccess(success: Result<List<GuardianGroup>>) {
		liveData.value = success
	}
	
}