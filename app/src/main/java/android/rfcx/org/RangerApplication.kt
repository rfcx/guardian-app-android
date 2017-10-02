package android.rfcx.org

import android.app.Application
import android.rfcx.org.ranger.BuildConfig
import com.facebook.stetho.Stetho

/**
 * Created by Jingjoeh on 10/2/2017 AD.
 */
class RangerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_STETHO) {
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .build())
        }
    }


}