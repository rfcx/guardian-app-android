package org.rfcx.ranger

import android.app.Application
import com.facebook.stetho.Stetho
import io.realm.Realm
import io.realm.RealmConfiguration
import org.rfcx.ranger.service.ReportCleanupWorker

class RangerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(config)

        ReportCleanupWorker.enqueuePeriodically()

        if (BuildConfig.USE_STETHO) {
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build())
        }
    }

}