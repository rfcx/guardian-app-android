package org.rfcx.incidents.domain

import io.realm.Realm
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.DetectionsRepositoryImp
import org.rfcx.incidents.data.EventsRepositoryImpl
import org.rfcx.incidents.data.MediaRepositoryImp
import org.rfcx.incidents.data.PasswordChangeRepositoryImp
import org.rfcx.incidents.data.ProfilePhotoRepositoryImp
import org.rfcx.incidents.data.ProjectsRepositoryImp
import org.rfcx.incidents.data.SetNameRepositoryImp
import org.rfcx.incidents.data.StreamsRepositoryImp
import org.rfcx.incidents.data.SubscribeRepositoryImp
import org.rfcx.incidents.data.UserTouchRepositoryImp
import org.rfcx.incidents.data.interfaces.CreateResponseRepository
import org.rfcx.incidents.data.interfaces.DetectionsRepository
import org.rfcx.incidents.data.interfaces.EventsRepository
import org.rfcx.incidents.data.interfaces.MediaRepository
import org.rfcx.incidents.data.interfaces.PasswordChangeRepository
import org.rfcx.incidents.data.interfaces.ProfilePhotoRepository
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.data.interfaces.SetNameRepository
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.interfaces.SubscribeRepository
import org.rfcx.incidents.data.interfaces.UserTouchRepository
import org.rfcx.incidents.data.local.AssetDb
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.preferences.CredentialKeeper
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.CredentialVerifier
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.view.UiThread

object DataModule {

    val dataModule = module {

        factory { JobExecutor() } bind ThreadExecutor::class
        factory { UiThread() } bind PostExecutionThread::class

        single { ProjectsRepositoryImp(get(), get(), get(), get()) } bind ProjectsRepository::class
        single { GetProjectsUseCase(get(), get(), get()) }

        single { StreamsRepositoryImp(get(), get(), get(), get(), get()) } bind StreamsRepository::class
        single { GetStreamsUseCase(get(), get(), get()) }

        single { EventsRepositoryImpl(get()) } bind EventsRepository::class
        single { GetEventsUseCase(get(), get(), get()) }

        single { DetectionsRepositoryImp(get()) } bind DetectionsRepository::class
        single { GetDetectionsUseCase(get(), get(), get()) }

        single { PasswordChangeRepositoryImp(get()) } bind PasswordChangeRepository::class
        single { PasswordChangeUseCase(get(), get(), get()) }

        single { UserTouchRepositoryImp(get()) } bind UserTouchRepository::class
        single { CheckUserTouchUseCase(get(), get(), get()) }

        single { SetNameRepositoryImp(get()) } bind SetNameRepository::class
        single { SendNameUseCase(get(), get(), get()) }

        single { org.rfcx.incidents.data.CreateResponseRepositoryImpl(get()) } bind CreateResponseRepository::class
        single { CreateResponseUseCase(get(), get(), get()) }

        single { ProfilePhotoRepositoryImp(get()) } bind ProfilePhotoRepository::class
        single { ProfilePhotoUseCase(get(), get(), get()) }

        single { SubscribeRepositoryImp(get()) } bind SubscribeRepository::class
        single { SubscribeUseCase(get(), get(), get()) }

        single { MediaRepositoryImp(get()) } bind MediaRepository::class
        single { MediaUseCase(get(), get(), get()) }
    }

    val remoteModule = module {
        factory { ServiceFactory.makeProjectsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeStreamsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeDetectionsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeMediaService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeAssetsService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeUserTouchService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSetNameService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makePasswordService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeProfilePhotoService(BuildConfig.DEBUG, androidContext()) }
        factory { ServiceFactory.makeSubscribeService(BuildConfig.DEBUG, androidContext()) }
    }

    val localModule = module {
        factory<Realm> { Realm.getInstance(AppRealm.configuration()) }
        factory { CachedEndpointDb(get()) }
        factory { ProjectDb(get()) }
        factory { ResponseDb(get()) }
        factory { EventDb(get()) }
        factory { StreamDb(get()) }
        factory { AssetDb(get()) }
        factory { TrackingDb(get()) }
        factory { ProfileData(get()) }
        factory { Preferences.getInstance(androidContext()) }
        single { CredentialKeeper(androidContext()) }
        single { CredentialVerifier(androidContext()) }
    }
}
