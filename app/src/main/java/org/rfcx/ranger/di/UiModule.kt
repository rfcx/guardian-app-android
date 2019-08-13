package org.rfcx.ranger.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.alert.AlertBottomDialogViewModel
import org.rfcx.ranger.view.alerts.AlertsViewModel
import org.rfcx.ranger.view.map.MapViewModel
import org.rfcx.ranger.view.map.ReportDetailViewModel
import org.rfcx.ranger.view.status.StatusViewModel

object UiModule {

	val mapModule = module {
		viewModel { MapViewModel(get(), get()) }
		viewModel { ReportDetailViewModel(get(), get()) }
	}

	val statusModule = module {
		viewModel { StatusViewModel(get(), get()) }
	}

	val alertModule = module {
		viewModel { AlertsViewModel() }
		viewModel { AlertBottomDialogViewModel() }
	}
}