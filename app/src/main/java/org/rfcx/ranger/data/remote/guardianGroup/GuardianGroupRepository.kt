package org.rfcx.ranger.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GuardianGroup

interface GuardianGroupRepository {
	fun getGuardianGroups(): Single<List<GuardianGroup>>
}