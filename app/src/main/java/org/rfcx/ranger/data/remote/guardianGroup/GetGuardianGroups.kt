package org.rfcx.ranger.data.remote.guardianGroup

import android.util.Log
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.CachedEndpointDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.localdb.SiteGuardianDb
import org.rfcx.ranger.util.Preferences

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
				cachedEndpointDb.updateCachedEndpoint(endpoint)
				
				callback.onSuccess(t)
			}
			
			override fun onError(e: Throwable) {
				callback.onError(e)
			}
		}, params)
	}
}