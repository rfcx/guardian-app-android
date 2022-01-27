package org.rfcx.incidents.di

import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.JobExecutor
import org.rfcx.incidents.UiThread
import org.rfcx.incidents.data.api.detections.DetectionsRepository
import org.rfcx.incidents.data.api.detections.DetectionsRepositoryImp
import org.rfcx.incidents.data.api.detections.GetDetections
import org.rfcx.incidents.data.api.events.EventsRepository
import org.rfcx.incidents.data.api.events.EventsRepositoryImp
import org.rfcx.incidents.data.api.events.GetEvents
import org.rfcx.incidents.data.api.media.MediaRepository
import org.rfcx.incidents.data.api.media.MediaRepositoryImp
import org.rfcx.incidents.data.api.media.MediaUseCase
import org.rfcx.incidents.data.api.project.GetProjectsRepository
import org.rfcx.incidents.data.api.project.GetProjectsRepositoryImp
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.streams.GetStreamsRepository
import org.rfcx.incidents.data.api.streams.GetStreamsRepositoryImp
import org.rfcx.incidents.data.api.streams.GetStreamsUseCase
import org.rfcx.incidents.data.local.*
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
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
        
        single { DetectionsRepositoryImp(get()) } bind DetectionsRepository::class
        single { GetDetections(get(), get(), get()) }
        
        single { PasswordChangeRepositoryImp(get()) } bind PasswordChangeRepository::class
        single { PasswordChangeUseCase(get(), get(), get()) }
        
        single { UserTouchRepositoryImp(get()) } bind UserTouchRepository::class
        single { CheckUserTouchUseCase(get(), get(), get()) }
        
        single { SetNameRepositoryImp(get()) } bind SetNameRepository::class
        single { SendNameUseCase(get(), get(), get()) }
        
        single { SiteRepositoryImp(get()) } bind SiteRepository::class
        single { GetSiteNameUseCase(get(), get(), get()) }
        
        single { CreateResponseRepositoryImp(get()) } bind CreateResponseRepository::class
        single { CreateResponse(get(), get(), get()) }
        
        single { ProfilePhotoRepositoryImp(get()) } bind ProfilePhotoRepository::class
        single { ProfilePhotoUseCase(get(), get(), get()) }
        
        single { SubscribeRepositoryImp(get()) } bind SubscribeRepository::class
        single { SubscribeUseCase(get(), get(), get()) }
        
        single { UnsubscribeRepositoryImp(get()) } bind UnsubscribeRepository::class
        single { UnsubscribeUseCase(get(), get(), get()) }
        
        single { MediaRepositoryImp(get()) } bind MediaRepository::class
        single { MediaUseCase(get(), get(), get()) }
        
        single { TermsRepositoryImp(get()) } bind TermsRepository::class
        single { TermsUseCase(get(), get(), get()) }
        
    }
    
    val remoteModule = module {
        factory { ServiceFactory.makeProjectsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeStreamsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeDetectionsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeMediaService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeEventsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeAssetsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeUserTouchService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSetNameService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSiteNameService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makePasswordService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeProfilePhotoService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSubscribeService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeTermsService(BuildConfig.DEBUG, androidContext()) }
    }
    
    val localModule = module {
        factory<Realm> { Realm.getInstance(RealmHelper.migrationConfig()) }
        factory { CachedEndpointDb(get()) }
        factory { ProjectDb(get()) }
        factory { LocationDb(get()) }
        factory { ReportDb(get()) }
        factory { ResponseDb(get()) }
        factory { ReportImageDb(get()) }
        factory { VoiceDb(get()) }
        factory { AlertDb(get()) }
        factory { StreamDb(get()) }
        factory { TrackingDb(get()) }
        factory { TrackingFileDb(get()) }
        factory { WeeklySummaryData(get()) }
        factory { ProfileData(get()) }
        factory { Preferences.getInstance(androidContext()) }
        single { CredentialKeeper(androidContext()) }
    }
}
