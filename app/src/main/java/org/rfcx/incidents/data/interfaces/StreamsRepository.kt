package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.entity.stream.Stream

interface StreamsRepository {
    fun get(params: GetStreamsParams): Single<List<Stream>>

    fun getLocalAsFlow(params: GetLocalStreamsParams): Flow<List<Stream>>

    fun getLocal(params: GetLocalStreamsParams): List<Stream>
}
