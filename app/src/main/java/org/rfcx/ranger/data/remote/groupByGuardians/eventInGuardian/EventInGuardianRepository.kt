package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventInGuardianResponse

interface EventInGuardianRepository {
	fun getEventInGuardian(sendBody: String): Single<EventInGuardianResponse>
}