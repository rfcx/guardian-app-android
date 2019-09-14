package org.rfcx.ranger.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GuardianGroupRepositoryImp(private val groupService: GuardianGroupEndpoint) : GuardianGroupRepository {
	override fun getGuardianGroups(): Single<List<GuardianGroup>> {
		return groupService.guardianGroups()
	}
}