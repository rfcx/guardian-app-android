package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityViewModel
import org.rfcx.ranger.view.alert.AlertBottomDialogViewModel
import org.rfcx.ranger.view.alerts.AlertViewModel
import org.rfcx.ranger.view.alerts.AllAlertsViewModel
import org.rfcx.ranger.view.alerts.GroupAlertsViewModel
import org.rfcx.ranger.view.alerts.guardian.GuardianViewModel
import org.rfcx.ranger.view.alerts.guardian.alertType.AlertValueViewModel
import org.rfcx.ranger.view.login.InvitationCodeViewModel
import org.rfcx.ranger.view.login.LoginViewModel
import org.rfcx.ranger.view.login.SetUserNameViewModel
import org.rfcx.ranger.view.map.MapDetailViewModel
import org.rfcx.ranger.view.map.MapViewModel
import org.rfcx.ranger.view.map.ReportViewPagerFragmentViewModel
import org.rfcx.ranger.view.profile.FeedbackViewModel
import org.rfcx.ranger.view.profile.GuardianGroupViewModel
import org.rfcx.ranger.view.profile.PasswordChangeViewModel
import org.rfcx.ranger.view.profile.ProfileViewModel
import org.rfcx.ranger.view.report.ReportDetailViewModel
import org.rfcx.ranger.view.status.StatusViewModel

object UiModule {
	
	val mainModule = module {
		viewModel { LocationTrackingViewModel(get()) }
		viewModel { MainActivityViewModel(get(), get()) }
	}
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get()) }
		viewModel { MapDetailViewModel(get(), get()) }
		viewModel { ReportViewPagerFragmentViewModel(get()) }
	}
	
	val statusModule = module {
		viewModel {
			StatusViewModel(androidContext(), get(), get(), get(), get(), get(), get(),
					get(), get())
		}
		viewModel { ReportDetailViewModel(get(), get(), get()) }
	}
	
	val alertModule = module {
		viewModel { AllAlertsViewModel(androidContext(), get(), get(), get(), get()) }
		viewModel { AlertBottomDialogViewModel(androidContext(), get(), get(), get()) }
		viewModel { GroupAlertsViewModel(androidContext(), get(), get()) }
		viewModel { GuardianViewModel(get()) }
		viewModel { AlertViewModel(androidContext(), get(), get()) }
		viewModel { AlertValueViewModel(androidContext(), get(), get()) }
	}
	
	val profileModule = module {
		viewModel { ProfileViewModel(androidContext(), get(), get(), get()) }
		viewModel { GuardianGroupViewModel(androidContext(), get(), get(), get(), get()) }
		viewModel { FeedbackViewModel(androidContext()) }
		viewModel { PasswordChangeViewModel(get()) }
	}
	
	var loginModule = module {
		viewModel { LoginViewModel(androidContext(), get()) }
		viewModel { InvitationCodeViewModel(androidContext(), get()) }
		viewModel { SetUserNameViewModel(androidContext(), get()) }
	}
}