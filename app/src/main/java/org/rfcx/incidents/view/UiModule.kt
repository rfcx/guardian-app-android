package org.rfcx.incidents.view

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.incidents.view.events.StreamsViewModel
import org.rfcx.incidents.view.events.detail.EventViewModel
import org.rfcx.incidents.view.events.detail.StreamDetailViewModel
import org.rfcx.incidents.view.guardian.GuardianDeploymentViewModel
import org.rfcx.incidents.view.guardian.checklist.GuardianCheckListViewModel
import org.rfcx.incidents.view.guardian.checklist.audio.GuardianAudioParameterFragment
import org.rfcx.incidents.view.guardian.checklist.audio.GuardianAudioParameterViewModel
import org.rfcx.incidents.view.guardian.checklist.checkin.GuardianCheckinTestViewModel
import org.rfcx.incidents.view.guardian.checklist.classifierupload.ClassifierUploadViewModel
import org.rfcx.incidents.view.guardian.checklist.communication.CommunicationViewModel
import org.rfcx.incidents.view.guardian.checklist.microphone.GuardianMicrophoneViewModel
import org.rfcx.incidents.view.guardian.checklist.network.NetworkTestViewModel
import org.rfcx.incidents.view.guardian.checklist.photos.AddPhotosViewModel
import org.rfcx.incidents.view.guardian.checklist.powerdiagnostic.PowerDiagnosticViewModel
import org.rfcx.incidents.view.guardian.checklist.preference.GuardianPreferenceViewModel
import org.rfcx.incidents.view.guardian.checklist.registration.GuardianRegisterViewModel
import org.rfcx.incidents.view.guardian.checklist.site.GuardianSiteSelectViewModel
import org.rfcx.incidents.view.guardian.checklist.site.GuardianSiteSetViewModel
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateViewModel
import org.rfcx.incidents.view.guardian.checklist.storage.GuardianStorageViewModel
import org.rfcx.incidents.view.guardian.checklist.storage.HeatmapAudioCoverageViewModel
import org.rfcx.incidents.view.guardian.connect.GuardianConnectViewModel
import org.rfcx.incidents.view.login.LoginViewModel
import org.rfcx.incidents.view.login.SetProjectsViewModel
import org.rfcx.incidents.view.login.SetUserNameViewModel
import org.rfcx.incidents.view.profile.FeedbackViewModel
import org.rfcx.incidents.view.profile.ProfileViewModel
import org.rfcx.incidents.view.profile.SubscribeProjectsViewModel
import org.rfcx.incidents.view.profile.guardian.GuardianFileDownloadViewModel
import org.rfcx.incidents.view.report.create.CreateReportViewModel
import org.rfcx.incidents.view.report.deployment.DeploymentListViewModel
import org.rfcx.incidents.view.report.detail.ResponseDetailViewModel

object UiModule {

    val mainModule = module {
        viewModel { MainActivityViewModel(get(), androidContext(), get(), get(), get(), get(), get(), get(), get()) }
    }

    val eventsModule = module {
        viewModel { StreamsViewModel(get(), get(), get(), get(), get()) }
        viewModel { EventViewModel(androidContext(), get(), get(), get(), get()) }
        viewModel { StreamDetailViewModel(get(), get(), get(), get()) }
    }

    val reportsModule = module {
        viewModel { CreateReportViewModel(get(), get(), get(), get(), get()) }
        viewModel { ResponseDetailViewModel(get(), get()) }
    }

    val profileModule = module {
        viewModel { ProfileViewModel(androidContext(), get(), get(), get()) }
        viewModel { SubscribeProjectsViewModel(get()) }
        viewModel { FeedbackViewModel(androidContext()) }
    }

    var loginModule = module {
        viewModel { LoginViewModel(androidContext(), get(), get()) }
        viewModel { SetUserNameViewModel(androidContext(), get()) }
        viewModel { SetProjectsViewModel(get()) }
    }

    var guardianModule = module {
        viewModel { GuardianDeploymentViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { GuardianConnectViewModel(get()) }
        viewModel { GuardianCheckListViewModel(get(), get(), get()) }
        viewModel { GuardianFileDownloadViewModel(androidContext(), get(), get(), get(), get()) }
        viewModel { SoftwareUpdateViewModel(get(), get(), get()) }
        viewModel { ClassifierUploadViewModel(get(), get(), get(), get()) }
        viewModel { PowerDiagnosticViewModel(get(), get()) }
        viewModel { NetworkTestViewModel(get(), get(), get()) }
        viewModel { CommunicationViewModel(get(), get(), get(), get(), get()) }
        viewModel { GuardianRegisterViewModel(get(), get(), get(), get()) }
        viewModel { GuardianAudioParameterViewModel(get(), get()) }
        viewModel { GuardianMicrophoneViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { GuardianStorageViewModel(androidContext(), get(), get()) }
        viewModel { HeatmapAudioCoverageViewModel() }
        viewModel { GuardianCheckinTestViewModel(get()) }
        viewModel { GuardianSiteSelectViewModel(get(), get(), get()) }
        viewModel { GuardianSiteSetViewModel(get(), get(), get()) }
        viewModel { AddPhotosViewModel(get()) }
        viewModel { GuardianPreferenceViewModel(androidContext(), get(), get()) }
        viewModel { DeploymentListViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    }
}
