package org.rfcx.ranger.view.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError

class GuardianGroupViewModel(private val context: Context, private val getGuardianGroups: GetGuardianGroups,
                             private val eventDb: EventDb,
                             private val subscribeUseCase: SubscribeUseCase,
                             private val unsubscribeUseCase: UnsubscribeUseCase) : ViewModel() {
	
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
	
	fun subscribeByEmail(guardianGroup: String) {
		val preference = Preferences.getInstance(context)
		val isSubscribe = preference.getBoolean(Preferences.EMAIL_SUBSCRIBE, false)
		
		if (isSubscribe) {
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					onSubscribe(guardianGroup)
				}
				
				override fun onError(e: Throwable) {
				}
			}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
		}
	}
	
	private fun onSubscribe(guardianGroup: String) {
		subscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
			}
			
			override fun onError(e: Throwable) {
			}
		}, SubscribeRequest(listOf(guardianGroup)))
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