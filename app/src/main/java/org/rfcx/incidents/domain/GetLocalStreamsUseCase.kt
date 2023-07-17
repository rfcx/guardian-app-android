package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetLocalStreamsUseCase(
    private val streamsRepository: StreamsRepository
) : FlowWithParamUseCase<GetLocalStreamsParams, List<Stream>>() {
    override fun performAction(param: GetLocalStreamsParams): Flow<List<Stream>> {
        if (param.needCopy) {
            return streamsRepository.listLocalCopyAsFlow(param)
        }
        return streamsRepository.listLocalAsFlow(param)
    }
}

data class GetLocalStreamsParams(
    val projectId: String,
    val needCopy: Boolean = false
)
