package org.rfcx.ranger.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.rfcx.ranger.view.map.MapViewModel

object UiModule {
	
	val mapModule = module {
		viewModel { MapViewModel(get(), get()) }
	}
}