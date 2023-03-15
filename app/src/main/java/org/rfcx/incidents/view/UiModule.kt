package org.rfcx.incidents.view

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.incidents.view.events.StreamsViewModel
import org.rfcx.incidents.view.events.detail.EventViewModel
import org.rfcx.incidents.view.events.detail.StreamDetailViewModel
import org.rfcx.incidents.view.guardian.GuardianDeploymentViewModel
import org.rfcx.incidents.view.guardian.checklist.GuardianCheckListViewModel
import org.rfcx.incidents.view.guardian.checklist.classifierupload.ClassifierUploadViewModel
import org.rfcx.incidents.view.guardian.checklist.network.NetworkTestViewModel
import org.rfcx.incidents.view.guardian.checklist.powerdiagnostic.PowerDiagnosticViewModel
import org.rfcx.incidents.view.guardian.checklist.softwareupdate.SoftwareUpdateViewModel
import org.rfcx.incidents.view.guardian.connect.GuardianConnectViewModel
import org.rfcx.incidents.view.login.LoginViewModel
import org.rfcx.incidents.view.login.SetProjectsViewModel
import org.rfcx.incidents.view.login.SetUserNameViewModel
import org.rfcx.incidents.view.profile.FeedbackViewModel
import org.rfcx.incidents.view.profile.ProfileViewModel
import org.rfcx.incidents.view.profile.SubscribeProjectsViewModel
import org.rfcx.incidents.view.profile.guardian.GuardianFileDownloadViewModel
import org.rfcx.incidents.view.report.create.CreateReportViewModel
import org.rfcx.incidents.view.report.detail.ResponseDetailViewModel

object UiModule {

    val mainModule = module {
        viewModel { MainActivityViewModel(get(), androidContext(), get(), get(), get(), get(), get(), get(), get()) }
    }

    val eventsModule = module {
        viewModel { StreamsViewModel(get(), get(), get(), get()) }
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
        viewModel { GuardianDeploymentViewModel(get(), get(), get(), get()) }
        viewModel { GuardianConnectViewModel(get(), get()) }
        viewModel { GuardianCheckListViewModel(androidContext()) }
        viewModel { GuardianFileDownloadViewModel(androidContext(), get(), get(), get(), get()) }
        viewModel { SoftwareUpdateViewModel(get(), get(), get()) }
        viewModel { ClassifierUploadViewModel(get(), get(), get(), get()) }
        viewModel { PowerDiagnosticViewModel(get(), get()) }
        viewModel { NetworkTestViewModel(get(), get(), get()) }
    }
}
