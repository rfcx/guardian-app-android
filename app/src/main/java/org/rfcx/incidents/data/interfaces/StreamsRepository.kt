package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.entity.stream.Stream

interface StreamsRepository {
    fun get(params: GetStreamsParams): Single<List<Stream>>
}
