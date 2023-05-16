package org.rfcx.incidents.view.guardian.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.local.common.Constants
import org.rfcx.incidents.data.remote.streams.realmList
import org.rfcx.incidents.domain.guardian.deploy.DeployDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentDeployParams
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.guardian.deployment.DeviceParameter
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.socket.PingUtils
import org.rfcx.incidents.util.socket.PingUtils.getGuardianToken
import org.rfcx.incidents.util.socket.PingUtils.getGuid
import org.rfcx.incidents.util.socket.PingUtils.isRegistered
import org.rfcx.incidents.view.guardian.GuardianScreen
import org.rfcx.incidents.view.guardian.checklist.photos.Image

class GuardianCheckListViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase,
    private val deployDeploymentUseCase: DeployDeploymentUseCase
) : ViewModel() {

    private val _checklistItemState: MutableStateFlow<List<CheckListItem>> = MutableStateFlow(emptyList())
    val checklistItemState = _checklistItemState.asStateFlow()

    private val _registrationState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val registrationState = _registrationState.asStateFlow()

    private var guid = ""
    private var token = ""
    private var guardianVital = ""

    init {
        getDeviceParameter()
        getGuardianRegistration()
    }

    private fun getDeviceParameter() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().combine(getGuardianMessageUseCase.launch()) { admin, guardian ->
                guardian?.getGuid()?.let {
                    guid = it
                }
                guardian?.getGuardianToken()?.let {
                    token = it
                }
                PingUtils.getGuardianVital(admin, guardian)?.let {
                    guardianVital = it
                }
            }
        }
    }

    private fun getGuardianRegistration() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.isRegistered()?.let {
                    _registrationState.tryEmit(it)
                }
            }
        }
    }

    fun getAllCheckList(passed: List<GuardianScreen>? = listOf()) {
        val checkList = arrayListOf<CheckListItem>()
        var number = 0

        checkList.add(CheckListItem.Header("Assembly"))
        Constants.GUARDIAN_ASSEMBLY_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header("Setup"))
        Constants.GUARDIAN_SETUP_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = true, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header("Optional"))
        Constants.GUARDIAN_OPTIONAL_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        _checklistItemState.tryEmit(checkList)
    }

    fun deploy(stream: Stream, images: List<Image>) {
        val deployment = Deployment(
            stream = stream,
            isActive = true,
            images = realmList(images.filter { it.path != null }.map {
                DeploymentImage(
                    localPath = it.path!!,
                    imageLabel = it.name
                )
            }),
            deviceParameters = Gson().toJson(DeviceParameter(guid, token, guardianVital))
        )
        deployDeploymentUseCase.launch(DeploymentDeployParams(deployment))
    }
}
