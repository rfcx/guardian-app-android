package org.rfcx.incidents.domain.guardian.socket

import com.google.gson.Gson
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionMessage
import org.rfcx.incidents.entity.guardian.socket.InstructionType

class SendInstructionCommandUseCase(
    private val guardianSocketRepository: GuardianSocketRepository
) : NoResultWithParamUseCase<InstructionParams>() {
    override fun performAction(param: InstructionParams) {
        val instruction = Gson().toJson(InstructionMessage.toMessage(param.type, param.command, param.meta))
        return guardianSocketRepository.sendMessage(instruction)
    }
}

data class InstructionParams(
    val type: InstructionType,
    val command: InstructionCommand,
    val meta: String = "{}"
)
