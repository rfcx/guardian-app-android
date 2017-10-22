package android.rfcx.org

import android.app.Application
import android.rfcx.org.ranger.BuildConfig
import com.facebook.stetho.Stetho
import io.realm.Realm
import io.realm.RealmConfiguration


/**
 * Created by Jingjoeh on 10/2/2017 AD.
 */
class RangerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        Realm.setDefaultConfiguration(config)

        if (BuildConfig.USE_STETHO) {
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build())
        }
    }


}