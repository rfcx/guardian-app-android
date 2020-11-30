package org.rfcx.ranger.view.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.util.getResultError

class SetProjectsViewModel(private val getProjects: GetGuardianGroups) : ViewModel() {
	private val _items = MutableLiveData<Result<List<GuardianGroup>>>()
	val items: LiveData<Result<List<GuardianGroup>>> get() = _items
	
	init {
		loadProjects()
	}
	
	private fun loadProjects() {
		_items.value = Result.Loading
		getProjects.execute(object : ResponseCallback<List<GuardianGroup>> {
			override fun onSuccess(t: List<GuardianGroup>) {
				_items.value = Result.Success(t)
			}
			
			override fun onError(e: Throwable) {
				_items.value = e.getResultError()
			}
			
		}, GuardianGroupFactory())
	}
}
