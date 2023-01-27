package org.rfcx.incidents.view.profile.guardian

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.software.GetSoftwareUseCase

class SoftwareDownloadViewModel(private val getSoftwareUseCase: GetSoftwareUseCase) : ViewModel() {

    fun getSoftwareFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            getSoftwareUseCase.launch().collect {
                Log.d("Comp", it.toString())
            }
        }
    }
}
