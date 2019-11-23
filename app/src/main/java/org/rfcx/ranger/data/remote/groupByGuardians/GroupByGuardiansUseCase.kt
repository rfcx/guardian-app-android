package org.rfcx.ranger.data.remote.groupByGuardians

import android.util.Log
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.CachedEndpointDb
import org.rfcx.ranger.data.local.GuardianDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.repo.ApiCallback

class GroupByGuardiansUseCase(private val groupByGuardiansRepository: GroupByGuardiansRepository,
                              private val cachedEndpointDb: CachedEndpointDb,
                              private val guardianDb: GuardianDb,
                              threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<String, GroupByGuardiansResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<GroupByGuardiansResponse> {
		return groupByGuardiansRepository.sendShortName(params)
	}
	
	fun execute(callback: ResponseCallback<List<Guardian>>,
	            params: String, force: Boolean = false) {
		val endpoint = "guardians/group/${params}"
		
		val result = cachedEndpointDb.hasCachedEndpoint(endpoint, 3)
		if (!force && result) {
			Log.d("GuardiansUseCase", "$endpoint -> used cached!")
			val guardians = guardianDb.getGuardians() ?: listOf()
			callback.onSuccess(guardians)
		}
		
		Log.d("GuardiansUseCase", "call $endpoint")
		
		this.execute(object : DisposableSingleObserver<GroupByGuardiansResponse>() {
			override fun onSuccess(t: GroupByGuardiansResponse) {
				// store guardians
				if (t.guardians.isNotEmpty()) {
					guardianDb.saveGuardians(t.guardians)
				}
				
				// cache endpoint
				cachedEndpointDb.updateCachedEndpoint(endpoint)
				
				callback.onSuccess(t.guardians)
			}
			
			override fun onError(e: Throwable) {
				callback.onError(e)
			}
		}, params)
	}
}