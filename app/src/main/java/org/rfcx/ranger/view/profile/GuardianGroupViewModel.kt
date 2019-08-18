package org.rfcx.ranger.view.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GuardianGroupViewModel(private val getGuardianGroups: GetGuardianGroups) : ViewModel() {
	
	private val _items = MutableLiveData<Result<List<GuardianGroup>>>()
	val items: LiveData<Result<List<GuardianGroup>>> get() = _items
	
	init {
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		_items.value = Result.Loading
		getGuardianGroups.execute(object : DisposableSingleObserver<List<GuardianGroup>>() {
			override fun onSuccess(t: List<GuardianGroup>) {
				_items.value = Result.Success(t)
				
			}
			
			override fun onError(e: Throwable) {
				_items.value = Result.Error(e)
			}
		}, GuardianGroupFactory())
	}
}