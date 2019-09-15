package org.rfcx.ranger.view.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.entity.event.Event

class AlertViewModel(private val profileData: ProfileData) : ViewModel() {
	
	var hasGuardianGroup = profileData.hasGuardianGroup()
	
	val eventFromNotification = MutableLiveData<Event>()
	
	private val _observeGuardianGroup = MutableLiveData<Boolean>()
	val observeGuardianGroup: LiveData<Boolean> = _observeGuardianGroup
	
	fun resumed() {
		hasGuardianGroup =  profileData.hasGuardianGroup()
		_observeGuardianGroup.value = profileData.hasGuardianGroup()
	}
}