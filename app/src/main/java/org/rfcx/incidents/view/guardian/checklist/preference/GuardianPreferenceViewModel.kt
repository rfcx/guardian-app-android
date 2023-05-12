package org.rfcx.incidents.view.guardian.checklist.preference

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.Preference
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.util.socket.PingUtils.getPrefs
import org.rfcx.incidents.util.socket.PingUtils.getPrefsSha1

class GuardianPreferenceViewModel(
    private val context: Context,
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase
) : ViewModel() {

    private val _preferenceState: MutableStateFlow<List<Preference>> = MutableStateFlow(emptyList())
    val preferenceState = _preferenceState.asStateFlow()

    private val _syncState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val syncState = _syncState.asStateFlow()

    private val prefsChanges = mutableMapOf<String, String>()

    private var needCheckSha1 = false
    private var currentGuardianSha1 = ""
    private var isFirstTime = true

    init {
        getCurrentPreferences()
        getPrefsSha1()
    }

    private fun getCurrentPreferences() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getPrefs(context)?.let {
                    if (isFirstTime) {
                        _preferenceState.tryEmit(it)
                        isFirstTime = false
                    }
                }
            }
        }
    }

    private fun getPrefsSha1() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getPrefsSha1()?.let {
                    if (needCheckSha1 && currentGuardianSha1 != it) {
                        _syncState.tryEmit(true)
                        needCheckSha1 = false
                    }
                    currentGuardianSha1 = it
                }
            }
        }
    }

    fun setPreferencesChanged(key: String, value: String) {
        prefsChanges[key] = value
    }

    fun sync() {
        needCheckSha1 = true
        _syncState.tryEmit(false)
        viewModelScope.launch(Dispatchers.IO) {
            sendInstructionCommandUseCase.launch(InstructionParams(InstructionType.SET, InstructionCommand.PREFS, prefsToGuardianFormat().toString()))
        }
    }

    private fun prefsToGuardianFormat(): JsonObject {
        val json = JsonObject()
        if (this.prefsChanges.isNotEmpty()) {
            this.prefsChanges.forEach {
                json.addProperty(it.key, it.value)
            }
        }
        return json
    }
}
