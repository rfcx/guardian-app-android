package org.rfcx.incidents.di

import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.JobExecutor
import org.rfcx.incidents.UiThread
import org.rfcx.incidents.data.api.events.EventsRepository
import org.rfcx.incidents.data.api.events.EventsRepositoryImp
import org.rfcx.incidents.data.api.events.GetEvents
import org.rfcx.incidents.data.api.project.GetProjectsRepository
import org.rfcx.incidents.data.api.project.GetProjectsRepositoryImp
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.site.GetStreamsRepository
import org.rfcx.incidents.data.api.site.GetStreamsRepositoryImp
import org.rfcx.incidents.data.api.site.GetStreamsUseCase
import org.rfcx.incidents.data.local.*
import org.rfcx.incidents.data.remote.data.alert.EventRepository
import org.rfcx.incidents.data.remote.data.classified.ClassifiedRepository
import org.rfcx.incidents.data.remote.domain.alert.EventRepositoryImp
import org.rfcx.incidents.data.remote.domain.alert.GetEventUseCase
import org.rfcx.incidents.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.incidents.data.remote.domain.alert.ReviewEventUseCase
import org.rfcx.incidents.data.remote.domain.classified.ClassifiedRepositoryImp
import org.rfcx.incidents.data.remote.domain.classified.GetClassifiedUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.data.remote.guardianGroup.GetGuardianGroups
import org.rfcx.incidents.data.remote.guardianGroup.GuardianGroupRepository
import org.rfcx.incidents.data.remote.guardianGroup.GuardianGroupRepositoryImp
import org.rfcx.incidents.data.remote.invitecode.InviteCodeRepository
import org.rfcx.incidents.data.remote.invitecode.InviteCodeRepositoryImp
import org.rfcx.incidents.data.remote.invitecode.SendInviteCodeUseCase
import org.rfcx.incidents.data.remote.password.PasswordChangeRepository
import org.rfcx.incidents.data.remote.password.PasswordChangeRepositoryImp
import org.rfcx.incidents.data.remote.password.PasswordChangeUseCase
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoRepository
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoRepositoryImp
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoUseCase
import org.rfcx.incidents.data.remote.response.CreateResponse
import org.rfcx.incidents.data.remote.response.CreateResponseRepository
import org.rfcx.incidents.data.remote.response.CreateResponseRepositoryImp
import org.rfcx.incidents.data.remote.service.ServiceFactory
import org.rfcx.incidents.data.remote.setusername.SendNameUseCase
import org.rfcx.incidents.data.remote.setusername.SetNameRepository
import org.rfcx.incidents.data.remote.setusername.SetNameRepositoryImp
import org.rfcx.incidents.data.remote.shortlink.ShortLinkRepository
import org.rfcx.incidents.data.remote.shortlink.ShortLinkRepositoryImp
import org.rfcx.incidents.data.remote.shortlink.ShortLinkUseCase
import org.rfcx.incidents.data.remote.site.GetSiteNameUseCase
import org.rfcx.incidents.data.remote.site.SiteRepository
import org.rfcx.incidents.data.remote.site.SiteRepositoryImp
import org.rfcx.incidents.data.remote.subscribe.SubscribeRepository
import org.rfcx.incidents.data.remote.subscribe.SubscribeRepositoryImp
import org.rfcx.incidents.data.remote.subscribe.SubscribeUseCase
import org.rfcx.incidents.data.remote.subscribe.unsubscribe.UnsubscribeRepository
import org.rfcx.incidents.data.remote.subscribe.unsubscribe.UnsubscribeRepositoryImp
import org.rfcx.incidents.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.incidents.data.remote.terms.TermsRepository
import org.rfcx.incidents.data.remote.terms.TermsRepositoryImp
import org.rfcx.incidents.data.remote.terms.TermsUseCase
import org.rfcx.incidents.data.remote.usertouch.CheckUserTouchUseCase
import org.rfcx.incidents.data.remote.usertouch.UserTouchRepository
import org.rfcx.incidents.data.remote.usertouch.UserTouchRepositoryImp
import org.rfcx.incidents.localdb.*
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.RealmHelper

object DataModule {
	
	val dataModule = module {
		
		factory { JobExecutor() } bind ThreadExecutor::class
		factory { UiThread() } bind PostExecutionThread::class
		
		single { GetProjectsRepositoryImp(get()) } bind GetProjectsRepository::class
		single { GetProjectsUseCase(get(), get(), get()) }
		
		single { GetStreamsRepositoryImp(get()) } bind GetStreamsRepository::class
		single { GetStreamsUseCase(get(), get(), get()) }
		
		single { EventsRepositoryImp(get()) } bind EventsRepository::class
		single { GetEvents(get(), get(), get()) }
		
		single { ClassifiedRepositoryImp(get()) } bind ClassifiedRepository::class
		single { GetClassifiedUseCase(get(), get(), get()) }
		
		single { EventRepositoryImp(get(), get(), get()) } bind EventRepository::class
		single { GetEventsUseCase(get(), get(), get(), get(), get(), get()) }
		single { ReviewEventUseCase(get(), get(), get()) }
		single { GetEventUseCase(get(), get(), get()) }
		
		single { GuardianGroupRepositoryImp(get()) } bind GuardianGroupRepository::class
		single { GetGuardianGroups(get(), get(), get(), get(), get()) }
		
		single { InviteCodeRepositoryImp(get()) } bind InviteCodeRepository::class
		single { SendInviteCodeUseCase(get(), get(), get()) }
		
		single { PasswordChangeRepositoryImp(get()) } bind PasswordChangeRepository::class
		single { PasswordChangeUseCase(get(), get(), get()) }
		
		single { UserTouchRepositoryImp(get()) } bind UserTouchRepository::class
		single { CheckUserTouchUseCase(get(), get(), get()) }
		
		single { SetNameRepositoryImp(get()) } bind SetNameRepository::class
		single { SendNameUseCase(get(), get(), get()) }
		
		single { SiteRepositoryImp(get()) } bind SiteRepository::class
		single { GetSiteNameUseCase(get(), get(), get()) }
		
		single { ShortLinkRepositoryImp(get()) } bind ShortLinkRepository::class
		single { ShortLinkUseCase(get(), get(), get()) }
		
		single { CreateResponseRepositoryImp(get()) } bind CreateResponseRepository::class
		single { CreateResponse(get(), get(), get()) }
		
		single { ProfilePhotoRepositoryImp(get()) } bind ProfilePhotoRepository::class
		single { ProfilePhotoUseCase(get(), get(), get()) }
		
		single { SubscribeRepositoryImp(get()) } bind SubscribeRepository::class
		single { SubscribeUseCase(get(), get(), get()) }
		
		single { UnsubscribeRepositoryImp(get()) } bind UnsubscribeRepository::class
		single { UnsubscribeUseCase(get(), get(), get()) }
		
		single { TermsRepositoryImp(get()) } bind TermsRepository::class
		single { TermsUseCase(get(), get(), get()) }
		
	}
	
	val remoteModule = module {
		factory { ServiceFactory.makeProjectsService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeStreamsService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeEventsService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeAssetsService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeClassifiedService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeEventService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeGuardianGroupService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeInviteCodeService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeUserTouchService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeSetNameService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeSiteNameService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeShortLinkService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makePasswordService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeProfilePhotoService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeSubscribeService(BuildConfig.DEBUG, androidContext()) }
		factory { ServiceFactory.makeTermsService(BuildConfig.DEBUG, androidContext()) }
	}
	
	val localModule = module {
		factory<Realm> { Realm.getInstance(RealmHelper.migrationConfig()) }
		factory { CachedEndpointDb(get()) }
		factory { GuardianDb(get()) }
		factory { ProjectDb(get()) }
		factory { GuardianGroupDb(get()) }
		factory { SiteGuardianDb(get()) }
		factory { LocationDb(get()) }
		factory { ReportDb(get()) }
		factory { ResponseDb(get()) }
		factory { ReportImageDb(get()) }
		factory { VoiceDb(get()) }
		factory { EventDb(get()) }
		factory { AlertDb(get()) }
		factory { StreamDb(get()) }
		factory { TrackingDb(get()) }
		factory { TrackingFileDb(get()) }
		factory { WeeklySummaryData(get()) }
		factory { ProfileData(get(), get()) }
		factory { Preferences.getInstance(androidContext()) }
		single { CredentialKeeper(androidContext()) }
	}
}
