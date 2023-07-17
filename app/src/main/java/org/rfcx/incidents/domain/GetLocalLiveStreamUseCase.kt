package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetLocalLiveStreamUseCase(
    private val streamsRepository: StreamsRepository
) : FlowWithParamUseCase<GetLocalStreamParams, Stream?>() {
    override fun performAction(param: GetLocalStreamParams): Flow<Stream?> {
        return streamsRepository.getByIdAsFlow(param.streamId)
    }
}
