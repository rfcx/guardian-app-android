package org.rfcx.incidents.domain

import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.JobExecutor
import org.rfcx.incidents.UiThread
import org.rfcx.incidents.data.CreateResponseRepository
import org.rfcx.incidents.data.CreateResponseRepositoryImp
import org.rfcx.incidents.data.PasswordChangeRepository
import org.rfcx.incidents.data.PasswordChangeRepositoryImp
import org.rfcx.incidents.data.ProfilePhotoRepository
import org.rfcx.incidents.data.ProfilePhotoRepositoryImp
import org.rfcx.incidents.data.ProjectsRepository
import org.rfcx.incidents.data.ProjectsRepositoryImp
import org.rfcx.incidents.data.SetNameRepository
import org.rfcx.incidents.data.SetNameRepositoryImp
import org.rfcx.incidents.data.SubscribeRepository
import org.rfcx.incidents.data.SubscribeRepositoryImp
import org.rfcx.incidents.data.UnsubscribeRepository
import org.rfcx.incidents.data.UnsubscribeRepositoryImp
import org.rfcx.incidents.data.UserTouchRepository
import org.rfcx.incidents.data.UserTouchRepositoryImp
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.LocationDb
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.local.ReportDb
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.data.local.VoiceDb
import org.rfcx.incidents.data.local.WeeklySummaryData
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.data.remote.detections.DetectionsRepository
import org.rfcx.incidents.data.remote.detections.DetectionsRepositoryImp
import org.rfcx.incidents.data.remote.detections.GetDetections
import org.rfcx.incidents.data.remote.events.EventsRepository
import org.rfcx.incidents.data.remote.events.EventsRepositoryImp
import org.rfcx.incidents.data.remote.events.GetEvents
import org.rfcx.incidents.data.remote.media.MediaRepository
import org.rfcx.incidents.data.remote.media.MediaRepositoryImp
import org.rfcx.incidents.data.remote.media.MediaUseCase
import org.rfcx.incidents.data.remote.streams.GetStreamsRepository
import org.rfcx.incidents.data.remote.streams.GetStreamsRepositoryImp
import org.rfcx.incidents.data.remote.streams.GetStreamsUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.RealmHelper

object DataModule {

    val dataModule = module {

        factory { JobExecutor() } bind ThreadExecutor::class
        factory { UiThread() } bind PostExecutionThread::class

        single { ProjectsRepositoryImp(get(), get(), get(), get()) } bind ProjectsRepository::class
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

        single { CreateResponseRepositoryImp(get()) } bind CreateResponseRepository::class
        single { CreateResponseUseCase(get(), get(), get()) }

        single { ProfilePhotoRepositoryImp(get()) } bind ProfilePhotoRepository::class
        single { ProfilePhotoUseCase(get(), get(), get()) }

        single { SubscribeRepositoryImp(get()) } bind SubscribeRepository::class
        single { SubscribeUseCase(get(), get(), get()) }

        single { UnsubscribeRepositoryImp(get()) } bind UnsubscribeRepository::class
        single { UnsubscribeUseCase(get(), get(), get()) }

        single { MediaRepositoryImp(get()) } bind MediaRepository::class
        single { MediaUseCase(get(), get(), get()) }
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
        factory { ServiceFactory.makePasswordService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeProfilePhotoService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSubscribeService(BuildConfig.DEBUG, androidContext()) }
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
