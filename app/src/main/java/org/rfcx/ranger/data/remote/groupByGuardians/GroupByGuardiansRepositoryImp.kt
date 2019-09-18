package org.rfcx.ranger.data.remote.groupByGuardians

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse

class GroupByGuardiansRepositoryImp(private val groupByGuardiansEndpoint: GroupByGuardiansEndpoint) : GroupByGuardiansRepository {
	override fun sendShortName(shortname: String): Single<GroupByGuardiansResponse> {
		return groupByGuardiansEndpoint.sendShortName(shortname)
	}
}