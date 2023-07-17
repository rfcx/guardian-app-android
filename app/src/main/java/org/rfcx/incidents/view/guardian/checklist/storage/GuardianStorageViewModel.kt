package org.rfcx.incidents.view.guardian.checklist.storage

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.entity.guardian.socket.GuardianArchived
import org.rfcx.incidents.util.socket.PingUtils.getGuardianArchivedAudios
import org.rfcx.incidents.util.socket.PingUtils.getStorage

class GuardianStorageViewModel(
    private val context: Context,
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase
) : ViewModel() {

    private val _internalState: MutableStateFlow<Int> = MutableStateFlow(-1)
    val internalState = _internalState.asStateFlow()

    private val _internalTextState: MutableStateFlow<String> = MutableStateFlow("")
    val internalTextState = _internalTextState.asStateFlow()

    private val _externalState: MutableStateFlow<Int> = MutableStateFlow(-1)
    val externalState = _externalState.asStateFlow()

    private val _externalTextState: MutableStateFlow<String> = MutableStateFlow("")
    val externalTextState = _externalTextState.asStateFlow()

    var archived = emptyList<GuardianArchived>()

    init {
        getStorage()
        getArchived()
    }

    private fun getStorage() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getStorage()?.let {
                    it.internal?.let { internal ->
                        val internalStorage = "${Formatter.formatFileSize(context, internal.used)}/${Formatter.formatFileSize(context, internal.all)}"
                        _internalTextState.tryEmit(internalStorage)
                        _internalState.tryEmit(((internal.used.toFloat() / internal.all.toFloat()) * 100).toInt())
                    }
                    it.external?.let { external ->
                        val externalStorage = "${Formatter.formatFileSize(context, external.used)}/${Formatter.formatFileSize(context, external.all)}"
                        _externalTextState.tryEmit(externalStorage)
                        _externalState.tryEmit(((external.used.toFloat() / external.all.toFloat()) * 100).toInt())
                    }
                }
            }
        }
    }

    private fun getArchived() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getGuardianArchivedAudios()?.let {
                    archived = it
                }
            }
        }
    }
}
