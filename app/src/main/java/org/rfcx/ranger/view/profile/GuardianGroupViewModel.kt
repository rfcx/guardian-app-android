package org.rfcx.ranger.view.profile

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.CachedEndpointDb
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError

class GuardianGroupViewModel(private val context: Context, private val getGuardianGroups: GetGuardianGroups,
                             private val eventDb: EventDb,
                             private val subscribeUseCase: SubscribeUseCase,
                             private val unsubscribeUseCase: UnsubscribeUseCase,
                             private val cachedEndpointDb: CachedEndpointDb) : ViewModel() {
	
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
	
	private fun subscribeByEmail(guardianGroup: String) {
		val preference = Preferences.getInstance(context)
		val isSubscribe = preference.getBoolean(Preferences.EMAIL_SUBSCRIBE, false)
		
		if (isSubscribe) {
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					onSubscribe(guardianGroup)
				}
				
				override fun onError(e: Throwable) {
					Toast.makeText(context, context.getString(R.string.error_unsubscribe_by_email,
							context.getGuardianGroup().toString()), Toast.LENGTH_SHORT).show()
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
	fun changeGuardianGroup(guardianGroup: GuardianGroup, callback: (Boolean) -> Unit) {
		eventDb.deleteAllEvents {
			if (it) {
				val preferences = Preferences.getInstance(context)
				preferences.putString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME, guardianGroup.name)
				
				// sub&unsub email
				subscribeByEmail(guardianGroup.shortname)
				
				// sub&unsub noti
				CloudMessaging.unsubscribe(context) {
					CloudMessaging.setProject(context, guardianGroup.shortname)
					CloudMessaging.subscribeIfRequired(context) {
						callback(true)
					}
				}
			} else {
				Toast.makeText(context, R.string.something_is_wrong, Toast.LENGTH_SHORT).show()
				callback(false)
			}
		}
	}
}
