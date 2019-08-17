package org.rfcx.ranger.view.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GuardianGroupViewModel(private val getGuardianGroups: GetGuardianGroups) : ViewModel() {
	private val _items = MutableLiveData<List<GuardianGroup>>()
	val items: LiveData<List<GuardianGroup>> get() = _items
	
	private val _loading = MutableLiveData<Boolean>()
	val loading: LiveData<Boolean> = _loading
	
	init {
		_loading.value = false
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		_loading.value = true
		getGuardianGroups.execute(object : DisposableSingleObserver<List<GuardianGroup>>() {
			override fun onSuccess(t: List<GuardianGroup>) {
				_items.value = t
				_loading.value = false
			}
			
			override fun onError(e: Throwable) {
				//TODO: handle error
				_loading.value = false
			}
		}, GuardianGroupFactory())
	}
}