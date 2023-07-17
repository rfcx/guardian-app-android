package org.rfcx.incidents.view.guardian.checklist.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterResponse
import org.rfcx.incidents.domain.guardian.registration.OfflineRegistrationParams
import org.rfcx.incidents.domain.guardian.registration.OnlineRegistrationParams
import org.rfcx.incidents.domain.guardian.registration.SaveRegistrationUseCase
import org.rfcx.incidents.domain.guardian.registration.SendRegistrationOnlineUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.guardian.registration.toSocketFormat
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.util.common.StringUtils
import org.rfcx.incidents.util.socket.PingUtils.getGuid
import org.rfcx.incidents.util.socket.PingUtils.isRegistered

class GuardianRegisterViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase,
    private val saveRegistrationUseCase: SaveRegistrationUseCase,
    private val sendRegistrationOnlineUseCase: SendRegistrationOnlineUseCase
) : ViewModel() {

    private val _registerTextState: MutableStateFlow<String> = MutableStateFlow("")
    val registerTextState = _registerTextState.asStateFlow()

    private val _registerButtonState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val registerButtonState = _registerButtonState.asStateFlow()

    private val _registrationState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val registrationState = _registrationState.asStateFlow()

    private var guid = ""
    private var waitingForRegistration = false

    init {
        getRegistration()
    }

    private fun getRegistration() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getGuid()?.let {
                    guid = it
                }
                result?.isRegistered()?.let {
                    if (it) {
                        _registrationState.tryEmit(true)
                        _registerButtonState.tryEmit(false)
                    } else {
                        _registrationState.tryEmit(false)
                    }
                    if (waitingForRegistration && it) {
                        waitingForRegistration = false
                    }
                    if (it && !waitingForRegistration) {
                        _registerTextState.tryEmit("Your Guardian is already registered")
                    } else if (!waitingForRegistration) {
                        _registerTextState.tryEmit("Your Guardian is not registered")
                    }
                }
            }
        }
    }

    fun sendRegistrationOffline(isProduction: Boolean) {
        viewModelScope.launch {
            val guid = this@GuardianRegisterViewModel.guid
            val token = StringUtils.generateSecureRandomHash(40)
            val pinCode = StringUtils.generateSecureRandomHash(4)
            val apiMqttHost = if (isProduction) "api-mqtt.rfcx.org" else "staging-api-mqtt.rfcx.org"
            val apiSmsAddress = if (isProduction) "+13467870964" else "+14154803657"
            val keystorePassphrase = if (isProduction) "x3bJwhSQ83A5ddkh" else "L2Cevkmc9W5fFCKn"
            if (guid.isNotEmpty()) {
                val registration = GuardianRegistration(
                    guid = guid,
                    token = token,
                    pinCode = pinCode,
                    apiMqttHost = apiMqttHost,
                    apiSmsAddress = apiSmsAddress,
                    keystorePassphrase = keystorePassphrase,
                    env = if (isProduction) "production" else "staging"
                )
                withContext(Dispatchers.Main) {
                    saveRegistrationUseCase.launch(OfflineRegistrationParams(registration))
                }
                withContext(Dispatchers.IO) {
                    waitingForRegistration = true
                    _registerButtonState.tryEmit(false)
                    _registerTextState.tryEmit("Registration Request Sent. Awaiting Response.")
                    sendInstructionCommandUseCase.launch(
                        InstructionParams(
                            InstructionType.SET,
                            InstructionCommand.IDENTITY,
                            convertToRegistrationFormat(registration)
                        )
                    )
                }
            }
        }
    }

    fun sendRegistrationOnline(isProduction: Boolean) {
        viewModelScope.launch {
            val env = if (isProduction) "production" else "staging"
            val guid = this@GuardianRegisterViewModel.guid
            if (guid.isNotEmpty()) {
                waitingForRegistration = true
                _registerButtonState.tryEmit(false)
                sendRegistrationOnlineUseCase.launch(OnlineRegistrationParams(env, GuardianRegisterRequest(guid, null, null))).collectLatest { result ->
                    when (result) {
                        is Result.Error -> _registerTextState.tryEmit("Register failed: ${result.throwable.message}")
                        Result.Loading -> _registerTextState.tryEmit("Registration Request Sent. Awaiting Response.")
                        is Result.Success -> {
                            withContext(Dispatchers.IO) {
                                sendInstructionCommandUseCase.launch(
                                    InstructionParams(
                                        InstructionType.SET,
                                        InstructionCommand.IDENTITY,
                                        convertToRegistrationFormat(result.data)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun convertToRegistrationFormat(registration: GuardianRegistration): String {
        return convertToRegistrationFormat(registration.toSocketFormat())
    }

    private fun convertToRegistrationFormat(registration: GuardianRegisterResponse): String {
        val renamedJson = JsonObject()
        renamedJson.addProperty("guid", registration.guid)
        renamedJson.addProperty("token", registration.token)
        renamedJson.addProperty("keystore_passphrase", registration.keystorePassphrase)
        renamedJson.addProperty("pin_code", registration.pinCode)
        renamedJson.addProperty("api_mqtt_host", registration.apiMqttHost)
        renamedJson.addProperty("api_sms_address", registration.apiSmsAddress)
        return Gson().toJson(renamedJson)
    }
}
