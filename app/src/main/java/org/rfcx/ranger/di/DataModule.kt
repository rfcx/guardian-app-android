package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.JobExecutor
import org.rfcx.ranger.UiThread
import org.rfcx.ranger.data.remote.data.classified.ClassifiedRepository
import org.rfcx.ranger.data.remote.domain.classified.ClassifiedRepositoryImp
import org.rfcx.ranger.data.remote.domain.classified.GetClassifiedUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.Preferences

object DataModule {
	
	val dataModule = module {
		
		factory { JobExecutor() } bind ThreadExecutor::class
		factory { UiThread() } bind PostExecutionThread::class
		
		single { ClassifiedRepositoryImp(get()) } bind ClassifiedRepository::class
		single { GetClassifiedUseCase(get(), get(), get()) }
	}
	
	val remoteModule = module {
		factory { ServiceFactory.makeClassifiedService(BuildConfig.DEBUG, androidContext()) }
	}
	
	val localModule = module {
		factory { LocationDb() }
		factory { ReportDb() }
		factory { ReportImageDb() }
		factory { Preferences.getInstance(androidContext()) }
	}
}