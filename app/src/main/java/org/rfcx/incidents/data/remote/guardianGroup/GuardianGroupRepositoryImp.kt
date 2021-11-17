package org.rfcx.incidents.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.incidents.entity.guardian.GuardianGroup

class GuardianGroupRepositoryImp(private val groupService: GuardianGroupEndpoint) : GuardianGroupRepository {
	override fun getGuardianGroups(): Single<List<GuardianGroup>> {
		return groupService.guardianGroups()
	}
}
