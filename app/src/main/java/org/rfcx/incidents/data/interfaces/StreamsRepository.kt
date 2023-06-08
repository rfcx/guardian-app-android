package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.entity.stream.Stream

interface StreamsRepository {
    fun list(params: GetStreamsParams): Single<List<Stream>>

    fun listLocalAsFlow(params: GetLocalStreamsParams): Flow<List<Stream>>

    fun listLocal(params: GetLocalStreamsParams): List<Stream>

    fun getById(id: Int): Stream?

    fun getByIdAsFlow(id: Int): Flow<Stream?>
}
