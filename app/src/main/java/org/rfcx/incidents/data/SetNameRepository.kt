package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse

interface SetNameRepository {
    fun sendName(sendBody: SetNameRequest): Single<SetNameResponse>
}
