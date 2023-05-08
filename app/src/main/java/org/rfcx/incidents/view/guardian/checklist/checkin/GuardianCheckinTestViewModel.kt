package org.rfcx.incidents.view.guardian.checklist.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.PingUtils.getLatestCheckIn
import org.rfcx.incidents.util.socket.PingUtils.getSwarmUnsetMessages
import org.rfcx.incidents.util.timestampToDateString

class GuardianCheckinTestViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase
) : ViewModel() {

    private val _protocolState: MutableStateFlow<String> = MutableStateFlow("-")
    val protocolState = _protocolState.asStateFlow()

    private val _lastCheckinState: MutableStateFlow<String> = MutableStateFlow("-")
    val lastCheckinState = _lastCheckinState.asStateFlow()

    private val _checkinQueueState: MutableStateFlow<String> = MutableStateFlow("-")
    val checkinQueueState = _checkinQueueState.asStateFlow()

    private val _checkinQueueVisibilityState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val checkinQueueVisibilityState = _checkinQueueVisibilityState.asStateFlow()

    private val _finishButtonState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val finishButtonState = _finishButtonState.asStateFlow()

    init {
        getCheckinData()
    }

    private fun getCheckinData() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getLatestCheckIn()?.let {
                    when {
                        it.has("mqtt") -> {
                            val mqtt = it.get("mqtt").asJsonObject
                            val createdAt = mqtt.get("created_at").asString

                            _protocolState.tryEmit("mqtt")
                            _lastCheckinState.tryEmit(createdAt)
                            _finishButtonState.tryEmit(true)
                        }
                        it.has("sbd") -> {
                            val sbd = it.get("sbd").asJsonObject
                            val createdAt = sbd.get("created_at").asString.toLongOrNull()

                            _protocolState.tryEmit("sbd")
                            _lastCheckinState.tryEmit(timestampToDateString(createdAt))
                            _finishButtonState.tryEmit(true)
                        }
                        it.has("swm") -> {
                            val swm = it.get("swm").asJsonObject
                            val unsent = result.getSwarmUnsetMessages() ?: -1
                            val createdAt = swm.get("created_at").asString.toLongOrNull()

                            _protocolState.tryEmit("swm")
                            _checkinQueueVisibilityState.tryEmit(true)
                            _lastCheckinState.tryEmit(timestampToDateString(createdAt))
                            _checkinQueueState.tryEmit(if (unsent != -1) "$unsent messages" else "unable to retrieve unsent message")
                            _finishButtonState.tryEmit(true)
                        }
                    }
                }
            }
        }
    }
}
