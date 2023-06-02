package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetLocalStreamUseCase(
    private val streamsRepository: StreamsRepository
) : FlowWithParamUseCase<GetLocalStreamParams, Stream?>() {
    override fun performAction(param: GetLocalStreamParams): Flow<Stream?> {
        return flow {
            emit(streamsRepository.getById(param.streamId))
        }
    }
}

data class GetLocalStreamParams(
    val streamId: Int
)
