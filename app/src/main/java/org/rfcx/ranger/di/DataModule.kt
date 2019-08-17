package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.JobExecutor
import org.rfcx.ranger.UiThread
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.data.classified.ClassifiedRepository
import org.rfcx.ranger.data.remote.domain.alert.EventRepositoryImp
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.data.remote.domain.alert.ReviewEventUseCase
import org.rfcx.ranger.data.remote.domain.classified.ClassifiedRepositoryImp
import org.rfcx.ranger.data.remote.domain.classified.GetClassifiedUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepository
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepositoryImp
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
		
		single { EventRepositoryImp(get(), get(), get()) } bind EventRepository::class
		single { GetEventsUseCase(get(), get(), get()) }
		single { ReviewEventUseCase(get(), get(), get()) }
		
		single { GuardianGroupRepositoryImp(get()) } bind GuardianGroupRepository::class
		single { GetGuardianGroups(get(), get(), get()) }
	}
	
	val remoteModule = module {
		factory { ServiceFactory.makeClassifiedService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeEventService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeGuardianGroupService(BuildConfig.DEBUG, androidContext()) }
	}
	
	val localModule = module {
		factory { LocationDb() }
		factory { ReportDb() }
		factory { ReportImageDb() }
		factory { EventDb() }
		factory { WeeklySummaryData(get()) }
		single { Preferences.getInstance(androidContext()) }
	}
}