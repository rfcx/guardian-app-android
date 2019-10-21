package org.rfcx.ranger.di

import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.JobExecutor
import org.rfcx.ranger.UiThread
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
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
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansRepository
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansRepositoryImp
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepository
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepositoryImp
import org.rfcx.ranger.data.remote.invitecode.InviteCodeRepository
import org.rfcx.ranger.data.remote.invitecode.InviteCodeRepositoryImp
import org.rfcx.ranger.data.remote.invitecode.SendInviteCodeUseCase
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.data.remote.setusername.SendNameUseCase
import org.rfcx.ranger.data.remote.setusername.SetNameRepository
import org.rfcx.ranger.data.remote.setusername.SetNameRepositoryImp
import org.rfcx.ranger.data.remote.usertouch.CheckUserTouchUseCase
import org.rfcx.ranger.data.remote.usertouch.UserTouchRepository
import org.rfcx.ranger.data.remote.usertouch.UserTouchRepositoryImp
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.RealmHelper

object DataModule {
	
	val dataModule = module {
		
		factory { JobExecutor() } bind ThreadExecutor::class
		factory { UiThread() } bind PostExecutionThread::class
		
		single { ClassifiedRepositoryImp(get()) } bind ClassifiedRepository::class
		single { GetClassifiedUseCase(get(), get(), get()) }
		
		single { EventRepositoryImp(get(), get(), get()) } bind EventRepository::class
		single { GetEventsUseCase(get(), get(), get()) }
		single { ReviewEventUseCase(get(), get(), get()) }
		single { GetMoreEventInGuardian(get(), get(), get()) }
		
		single { GuardianGroupRepositoryImp(get()) } bind GuardianGroupRepository::class
		single { GetGuardianGroups(get(), get(), get()) }
		
		single { InviteCodeRepositoryImp(get()) } bind InviteCodeRepository::class
		single { SendInviteCodeUseCase(get(), get(), get()) }
		
		single { UserTouchRepositoryImp(get()) } bind UserTouchRepository::class
		single { CheckUserTouchUseCase(get(), get(), get()) }
		
		single { SetNameRepositoryImp(get()) } bind SetNameRepository::class
		single { SendNameUseCase(get(), get(), get()) }
		
		single { GroupByGuardiansRepositoryImp(get()) } bind GroupByGuardiansRepository::class
		single { GroupByGuardiansUseCase(get(), get(), get()) }
		
	}
	
	val remoteModule = module {
		factory { ServiceFactory.makeClassifiedService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeEventService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeGuardianGroupService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeInviteCodeService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeUserTouchService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeSetNameService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeGroupByGuardiansService(BuildConfig.DEBUG, androidContext()) }
	}
	
	val localModule = module {
		factory<Realm> { Realm.getInstance(RealmHelper.migrationConfig())}
		factory { LocationDb(get()) }
		factory { ReportDb(get()) }
		factory { ReportImageDb(get()) }
		factory { EventDb(get()) }
		factory { WeeklySummaryData(get()) }
		factory { ProfileData(get()) }
		factory { Preferences.getInstance(androidContext()) }
		single { CredentialKeeper(androidContext()) }
	}
}