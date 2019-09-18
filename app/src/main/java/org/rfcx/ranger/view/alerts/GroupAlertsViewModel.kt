package org.rfcx.ranger.view.alerts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.util.Preferences

class GroupAlertsViewModel(private val context: Context, private val groupByGuardiansUseCase: GroupByGuardiansUseCase) : ViewModel() {
	
	private val _items = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val items: LiveData<Result<GroupByGuardiansResponse>> get() = _items
	
	init {
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		val preferenceHelper = Preferences.getInstance(context)
		val shortName = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
		_items.value = Result.Loading
		groupByGuardiansUseCase.execute(GetGroupByGuardianDisposable(_items), shortName.toString())
	}
}

class GetGroupByGuardianDisposable(
		private val liveData: MutableLiveData<Result<GroupByGuardiansResponse>>)
	: BaseDisposableSingle<GroupByGuardiansResponse>() {
	override fun onSuccess(success: Result<GroupByGuardiansResponse>) {
		liveData.value = success
	}
	
	override fun onError(e: Throwable, error: Result<GroupByGuardiansResponse>) {
		liveData.value = error
	}
}
