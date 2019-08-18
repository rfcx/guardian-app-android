package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.alert.AlertBottomDialogViewModel
import org.rfcx.ranger.view.alerts.AlertsViewModel
import org.rfcx.ranger.view.map.MapViewModel
import org.rfcx.ranger.view.map.ReportDetailViewModel
import org.rfcx.ranger.view.profile.GuardianGroupViewModel
import org.rfcx.ranger.view.profile.ProfileViewModel
import org.rfcx.ranger.view.status.StatusViewModel

object UiModule {
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get()) }
		viewModel { ReportDetailViewModel(get(), get()) }
	}
	
	val statusModule = module {
		viewModel { StatusViewModel(get(), get(), get(), get()) }
	}
	
	val alertModule = module {
		viewModel { AlertsViewModel(androidContext(), get(), get()) }
		viewModel { AlertBottomDialogViewModel(androidContext(), get(), get()) }
	}

	val profileModule = module{
		viewModel { ProfileViewModel(get()) }
		viewModel { GuardianGroupViewModel(get()) }
	}
}