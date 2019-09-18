package org.rfcx.ranger.data.remote.groupByGuardians

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse

interface GroupByGuardiansRepository {
	fun sendShortName(shortname: String): Single<GroupByGuardiansResponse>
}