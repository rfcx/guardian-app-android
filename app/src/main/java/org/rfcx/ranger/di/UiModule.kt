package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityViewModel
import org.rfcx.ranger.view.alert.AlertBottomDialogViewModel
import org.rfcx.ranger.view.alerts.AlertsViewModel
import org.rfcx.ranger.view.login.InvitationCodeViewModel
import org.rfcx.ranger.view.login.LoginViewModel
import org.rfcx.ranger.view.map.MapDetailViewModel
import org.rfcx.ranger.view.map.MapViewModel
import org.rfcx.ranger.view.profile.GuardianGroupViewModel
import org.rfcx.ranger.view.profile.ProfileViewModel
import org.rfcx.ranger.view.report.ReportDetailViewModel
import org.rfcx.ranger.view.status.StatusViewModel

object UiModule {
	
	val mainModule = module {
		viewModel { LocationTrackingViewModel(get()) }
		viewModel {MainActivityViewModel(get(),get())}
	}
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get()) }
		viewModel { MapDetailViewModel(get(), get()) }
	}
	
	val statusModule = module {
		viewModel { StatusViewModel(androidContext(), get(), get(), get(), get(), get()) }
		viewModel { ReportDetailViewModel(get(), get()) }
	}
	
	val alertModule = module {
		viewModel { AlertsViewModel(androidContext(), get(), get()) }
		viewModel { AlertBottomDialogViewModel(androidContext(), get(), get()) }
	}
	
	val profileModule = module {
		viewModel { ProfileViewModel(androidContext(), get()) }
		viewModel { GuardianGroupViewModel(get()) }
	}
	
	var loginModule = module {
		viewModel { LoginViewModel(androidContext(), get()) }
		viewModel { InvitationCodeViewModel(androidContext(), get()) }
	}
}