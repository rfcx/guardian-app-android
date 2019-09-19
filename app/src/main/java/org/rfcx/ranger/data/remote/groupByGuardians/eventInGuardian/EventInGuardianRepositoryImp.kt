package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventInGuardianResponse

class EventInGuardianRepositoryImp(private val eventInGuardianEndpoint: EventInGuardianEndpoint) : EventInGuardianRepository {
	override fun getEventInGuardian(sendBody: String): Single<EventInGuardianResponse> {
		return eventInGuardianEndpoint.sendGuardianName(sendBody)
	}
}