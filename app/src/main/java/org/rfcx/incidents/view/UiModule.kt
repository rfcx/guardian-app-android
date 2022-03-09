package org.rfcx.incidents.view

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.incidents.view.events.StreamsViewModel
import org.rfcx.incidents.view.events.detail.EventViewModel
import org.rfcx.incidents.view.events.detail.StreamDetailViewModel
import org.rfcx.incidents.view.login.LoginViewModel
import org.rfcx.incidents.view.login.SetProjectsViewModel
import org.rfcx.incidents.view.login.SetUserNameViewModel
import org.rfcx.incidents.view.profile.FeedbackViewModel
import org.rfcx.incidents.view.profile.ProfileViewModel
import org.rfcx.incidents.view.profile.SubscribeProjectsViewModel
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
        viewModel { CreateReportViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ResponseDetailViewModel(get(), get(), get(), get()) }
    }

    val profileModule = module {
        viewModel { ProfileViewModel(androidContext(), get(), get()) }
        viewModel { SubscribeProjectsViewModel(get()) }
        viewModel { FeedbackViewModel(androidContext()) }
    }

    var loginModule = module {
        viewModel { LoginViewModel(androidContext(), get(), get()) }
        viewModel { SetUserNameViewModel(androidContext(), get()) }
        viewModel { SetProjectsViewModel(get()) }
    }
}
