package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetLocalStreamsUseCase(
    private val streamsRepository: StreamsRepository
) : FlowWithParamUseCase<GetLocalStreamsParams, List<Stream>>() {
    override fun performAction(param: GetLocalStreamsParams): Flow<List<Stream>> {
        return streamsRepository.getLocalAsFlow(param)
    }
}

data class GetLocalStreamsParams(
    val projectId: String
)
