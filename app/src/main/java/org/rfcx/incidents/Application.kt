package org.rfcx.incidents

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.domain.DataModule
import org.rfcx.incidents.service.AirplaneModeReceiver
import org.rfcx.incidents.service.ResponseCleanupWorker
import org.rfcx.incidents.util.removeLocationUpdates
import org.rfcx.incidents.util.startLocationChange
import org.rfcx.incidents.view.UiModule

class Application : MultiDexApplication(), LifecycleObserver {

    private val onAirplaneModeCallback: (Boolean) -> Unit = { isOnAirplaneMode ->
        if (isOnAirplaneMode) {
            this.removeLocationUpdates()
        } else {
            this.startLocationChange()
        }
    }

    private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        MultiDex.install(this)

        setupPostHog()
        AppRealm.init(this)
        setupKoin()
        ResponseCleanupWorker.enqueuePeriodically()
        registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))

        if (BuildConfig.USE_STETHO) {
            Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build()
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppInForeground() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            this.startLocationChange()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppInBackground() {
        this.removeLocationUpdates()
    }

    private val listModules: ArrayList<Module> by lazy {
        arrayListOf(
            UiModule.mainModule,
            UiModule.eventsModule,
            UiModule.reportsModule,
            UiModule.profileModule,
            UiModule.loginModule,
            UiModule.guardianModule,
            DataModule.localModule,
            DataModule.remoteModule,
            DataModule.dataModule
        )
    }

    // Self-hosted PostHog product analytics (replaces Firebase Analytics).
    // Conservative config, consistent with the other rfcx clients: no screen-view
    // autocapture (screens are sent manually via Analytics.trackScreen), no deep
    // link autocapture, no session replay. App lifecycle events are kept.
    private fun setupPostHog() {
        val config = PostHogAndroidConfig(
            apiKey = BuildConfig.POSTHOG_API_KEY,
            host = BuildConfig.POSTHOG_HOST
        ).apply {
            captureScreenViews = false
            captureDeepLinks = false
            sessionReplay = false
            // We don't use PostHog feature flags/surveys here — skip the preload
            // network call (conservative). Event capture is unaffected.
            preloadFeatureFlags = false
        }
        PostHogAndroid.setup(this, config)
    }

    private fun setupKoin() {
        startKoin {
            androidContext(this@Application)
            modules(listModules)
        }
    }
}
