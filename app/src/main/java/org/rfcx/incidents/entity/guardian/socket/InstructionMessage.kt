package org.rfcx.incidents.entity.guardian.socket

import kotlin.random.Random

data class InstructionMessage(
    val instructions: List<Instruction>
) {
    companion object {
        fun toMessage(
            type: InstructionType,
            cmd: InstructionCommand,
            meta: String
        ): InstructionMessage {
            val instruction = Instruction(
                type = type.value,
                cmd = cmd.value,
                meta = meta
            )
            return InstructionMessage(listOf(instruction))
        }
    }
}

data class Instruction(
    val id: Int = Random.nextInt(1, 10000),
    val type: String,
    val cmd: String,
    val at: String = "",
    val meta: String = "{}"
)

enum class InstructionType(val value: String) {
    SET("set"),
    CTRL("ctrl"),
    SEND("send")
}

enum class InstructionCommand(val value: String) {
    PREFS("prefs"),
    WIFI("wifi"),
    PING("ping"),
    IDENTITY("identity"),
    SPEED_TEST("speed_test"),
    CLASSIFIER("classifier"),
    RESTART("restart")
}

data class ClassifierSet(
    val type: String,
    val id: String
)
