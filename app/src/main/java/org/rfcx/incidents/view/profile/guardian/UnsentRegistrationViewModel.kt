package org.rfcx.incidents.view.profile.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.registration.GetRegistrationUseCase
import org.rfcx.incidents.domain.guardian.registration.OnlineRegistrationParams
import org.rfcx.incidents.domain.guardian.registration.SendRegistrationOnlineUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.guardian.registration.toRequest

class UnsentRegistrationViewModel(
    private val getRegistrationUseCase: GetRegistrationUseCase,
    private val sendRegistrationOnlineUseCase: SendRegistrationOnlineUseCase
) : ViewModel() {

    private val _registrations: MutableStateFlow<List<GuardianRegistration>> = MutableStateFlow(emptyList())
    val registrations = _registrations.asStateFlow()

    private val _registrationError = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val registrationError = _registrationError.asSharedFlow()

    private val _noContentState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val noContentState = _noContentState.asStateFlow()

    init {
        getUnsentRegistrations()
    }

    private fun getUnsentRegistrations() {
        viewModelScope.launch(Dispatchers.Main) {
            getRegistrationUseCase.launch().collectLatest {
                _registrations.tryEmit(it)
                if (it.isEmpty()) {
                    _noContentState.tryEmit(true)
                } else {
                    _noContentState.tryEmit(false)
                }
            }
        }
    }

    fun register(registration: GuardianRegistration) {
        viewModelScope.launch {
            sendRegistrationOnlineUseCase.launch(OnlineRegistrationParams(registration.env, registration.toRequest())).collectLatest { result ->
                when (result) {
                    is Result.Error -> _registrationError.tryEmit(result.throwable.message ?: "There is something wrong while registering guardian")
                    Result.Loading -> {}
                    is Result.Success -> {}
                }
            }
        }
    }
}
