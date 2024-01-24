package org.rfcx.incidents.view.guardian.checklist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.streams.realmList
import org.rfcx.incidents.domain.guardian.deploy.DeploymentSaveParams
import org.rfcx.incidents.domain.guardian.deploy.SaveDeploymentUseCase
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.guardian.deployment.DeviceParameter
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.socket.PingUtils
import org.rfcx.incidents.util.socket.PingUtils.getGuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGuardianToken
import org.rfcx.incidents.util.socket.PingUtils.getGuid
import org.rfcx.incidents.util.socket.PingUtils.isRegistered
import org.rfcx.incidents.view.guardian.GuardianScreen
import org.rfcx.incidents.view.guardian.checklist.photos.Image
import java.util.Date

class GuardianCheckListViewModel(
    private val context: Context,
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase,
    private val saveDeploymentUseCase: SaveDeploymentUseCase
) : ViewModel() {

    private val _checklistItemState: MutableStateFlow<List<CheckListItem>> = MutableStateFlow(emptyList())
    val checklistItemState = _checklistItemState.asStateFlow()

    private val _registrationState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val registrationState = _registrationState.asStateFlow()

    private val _guardianIdState: MutableStateFlow<String> = MutableStateFlow("")
    val guardianIdState = _guardianIdState.asStateFlow()

    private var guid = ""
    private var token = ""
    private var guardianVital = ""
    private var guardianType = ""

    init {
        getDeviceParameter()
        getGuardianRegistration()
    }

    private fun getDeviceParameter() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().combine(getGuardianMessageUseCase.launch()) { admin, guardian ->
                guardian?.getGuid()?.let {
                    guid = it
                    _guardianIdState.tryEmit(it)
                }
                guardian?.getGuardianToken()?.let {
                    token = it
                }
                guardian?.getGuardianPlan()?.let {
                    guardianType = it.name
                }
                PingUtils.getGuardianVital(admin, guardian)?.let {
                    guardianVital = it
                }
            }.collect()
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

        checkList.add(CheckListItem.Header(context.getString(R.string.assembly)))
        context.resources.getStringArray(R.array.guardian_assembly_checks).forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header(context.getString(R.string.setup)))
        context.resources.getStringArray(R.array.guardian_setup_checks).forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = true, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header(context.getString(R.string.optional)))
        context.resources.getStringArray(R.array.guardian_optional_checks).forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        _checklistItemState.tryEmit(checkList)
    }

    fun deploy(stream: Stream, images: List<Image>) {
        val deployment = Deployment(
            isActive = true,
            images = realmList(
                images.filter { it.path != null }.map {
                    DeploymentImage(
                        localPath = it.path!!,
                        imageLabel = it.name
                    )
                }
            ),
            deployedAt = Date(),
            deviceParameters = Gson().toJson(DeviceParameter(guid, token, guardianType, guardianVital))
        )
        val newStream = Stream(
            id = stream.id,
            name = stream.name,
            latitude = stream.latitude,
            longitude = stream.longitude,
            altitude = stream.altitude,
            timezoneRaw = stream.timezoneRaw,
            projectId = stream.projectId,
            tags = stream.tags,
            lastIncident = stream.lastIncident,
            guardianType = stream.guardianType,
            order = stream.order,
            externalId = stream.externalId,
            syncState = stream.syncState,
            deployment = deployment
        )
        saveDeploymentUseCase.launch(DeploymentSaveParams(newStream))
    }
}
