package org.rfcx.incidents.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.incidents.entity.guardian.GuardianGroup

interface GuardianGroupRepository {
	fun getGuardianGroups(): Single<List<GuardianGroup>>
}
