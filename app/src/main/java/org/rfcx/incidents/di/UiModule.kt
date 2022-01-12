package org.rfcx.incidents.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.incidents.view.MainActivityViewModel
import org.rfcx.incidents.view.alert.AlertDetailViewModel
import org.rfcx.incidents.view.events.EventsViewModel
import org.rfcx.incidents.view.events.detail.GuardianEventDetailViewModel
import org.rfcx.incidents.view.login.LoginViewModel
import org.rfcx.incidents.view.login.SetProjectsViewModel
import org.rfcx.incidents.view.login.SetUserNameViewModel
import org.rfcx.incidents.view.map.MapDetailViewModel
import org.rfcx.incidents.view.map.MapViewModel
import org.rfcx.incidents.view.map.ReportViewPagerFragmentViewModel
import org.rfcx.incidents.view.profile.FeedbackViewModel
import org.rfcx.incidents.view.profile.GuardianGroupViewModel
import org.rfcx.incidents.view.profile.PasswordChangeViewModel
import org.rfcx.incidents.view.profile.ProfileViewModel
import org.rfcx.incidents.view.profile.editprofile.EditProfileViewModel
import org.rfcx.incidents.view.report.create.CreateReportViewModel
import org.rfcx.incidents.view.report.detail.ResponseDetailViewModel

object UiModule {
	
	val mainModule = module {
		viewModel { MainActivityViewModel(get(), get(), get()) }
	}
	
	val eventsModule = module {
		viewModel { EventsViewModel(androidContext(), get(), get(), get(), get(), get(), get(), get(), get()) }
		viewModel { AlertDetailViewModel(androidContext(), get(), get(), get()) }
		viewModel { GuardianEventDetailViewModel(get(), get(), get(), get()) }
	}
	
	val reportsModule = module {
		viewModel { CreateReportViewModel(get(), get(), get(), get(), get(), get()) }
		viewModel { ResponseDetailViewModel(get(), get(), get()) }
	}
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get(), get(), get(), get()) }
		viewModel { MapDetailViewModel(get(), get()) }
		viewModel { ReportViewPagerFragmentViewModel(get()) }
	}
	
	val profileModule = module {
		viewModel { ProfileViewModel(androidContext(), get(), get(), get(), get(), get()) }
		viewModel { GuardianGroupViewModel(androidContext(), get(), get()) }
		viewModel { FeedbackViewModel(androidContext()) }
		viewModel { PasswordChangeViewModel(get()) }
		viewModel { EditProfileViewModel(androidContext(), get()) }
	}
	
	var loginModule = module {
		viewModel { LoginViewModel(androidContext(), get()) }
		viewModel { SetUserNameViewModel(androidContext(), get()) }
		viewModel { SetProjectsViewModel(androidContext(), get(), get(), get(), get(), get()) }
	}
}
