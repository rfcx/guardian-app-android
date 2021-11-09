package org.rfcx.incidents.data.remote.guardianGroup

import android.util.Log
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.remote.ResponseCallback
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.GuardianGroupFactory
import org.rfcx.incidents.entity.guardian.GuardianGroup
import org.rfcx.incidents.localdb.SiteGuardianDb

class GetGuardianGroups(private val eventRepository: GuardianGroupRepository,
                        private val cachedEndpointDb: CachedEndpointDb,
                        private val siteGuardianDb: SiteGuardianDb,
                        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
		SingleUseCase<GuardianGroupFactory, List<GuardianGroup>>(threadExecutor, postExecutionThread) {
	
	override fun buildUseCaseObservable(params: GuardianGroupFactory): Single<List<GuardianGroup>> {
		return eventRepository.getGuardianGroups()
	}
	
	fun execute(callback: ResponseCallback<List<GuardianGroup>>, params: GuardianGroupFactory,
	            force: Boolean = false) {
		val endpoint = "v1/guardians/groups"
		
		if (!force && cachedEndpointDb.hasCachedEndpoint(endpoint, 24.0)) { // 1 day
			Log.d("GetGuardianGroups", "$endpoint -> used cached!")
			val guardianGroups = siteGuardianDb.guardianGroups()
			callback.onSuccess(guardianGroups)
			return
		}
		Log.d("GetGuardianGroups", "call $endpoint")
		
		this.execute(object : DisposableSingleObserver<List<GuardianGroup>>() {
			override fun onSuccess(t: List<GuardianGroup>) {
				siteGuardianDb.saveGuardianGroups(t)
				
				// cache endpoint
				if (t.isNotEmpty()) {
					cachedEndpointDb.updateCachedEndpoint(endpoint)
				}
				
				callback.onSuccess(t)
			}
			
			override fun onError(e: Throwable) {
				callback.onError(e)
			}
		}, params)
	}
}
