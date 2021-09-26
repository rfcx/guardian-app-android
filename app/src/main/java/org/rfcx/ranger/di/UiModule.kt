package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityViewModel
import org.rfcx.ranger.view.events.EventsViewModel
import org.rfcx.ranger.view.login.*
import org.rfcx.ranger.view.map.MapDetailViewModel
import org.rfcx.ranger.view.map.MapViewModel
import org.rfcx.ranger.view.map.ReportViewPagerFragmentViewModel
import org.rfcx.ranger.view.profile.FeedbackViewModel
import org.rfcx.ranger.view.profile.GuardianGroupViewModel
import org.rfcx.ranger.view.profile.PasswordChangeViewModel
import org.rfcx.ranger.view.profile.ProfileViewModel
import org.rfcx.ranger.view.profile.editprofile.EditProfileViewModel

object UiModule {
	
	val mainModule = module {
		viewModel { LocationTrackingViewModel(get()) }
		viewModel { MainActivityViewModel(get(), get(), get()) }
	}
	
	val eventsModule = module {
		viewModel { EventsViewModel(androidContext(), get(), get()) }
	}
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get(), get(), get(), get()) }
		viewModel { MapDetailViewModel(get(), get()) }
		viewModel { ReportViewPagerFragmentViewModel(get()) }
	}
	
	val profileModule = module {
		viewModel { ProfileViewModel(androidContext(), get(), get(), get(), get(), get()) }
		viewModel { GuardianGroupViewModel(androidContext(), get(), get(), get(), get(), get()) }
		viewModel { FeedbackViewModel(androidContext()) }
		viewModel { PasswordChangeViewModel(get()) }
		viewModel { EditProfileViewModel(androidContext(), get()) }
	}
	
	var loginModule = module {
		viewModel { LoginViewModel(androidContext(), get()) }
		viewModel { SetUserNameViewModel(androidContext(), get()) }
		viewModel { TermsAndServiceViewModel(androidContext(), get()) }
		viewModel { SetProjectsViewModel(androidContext(), get(), get(), get(), get()) }
	}
}
