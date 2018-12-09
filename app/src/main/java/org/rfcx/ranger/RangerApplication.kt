package org.rfcx.ranger

import android.app.Application
import com.facebook.stetho.Stetho
import io.realm.Realm
import org.rfcx.ranger.service.LocationCleanupWorker
import org.rfcx.ranger.service.ReportCleanupWorker
import org.rfcx.ranger.util.RealmHelper

class RangerApplication : Application() {
	
	override fun onCreate() {
		super.onCreate()
		
		Realm.init(this)
		Realm.setDefaultConfiguration(RealmHelper.defaultConfig())
		
		ReportCleanupWorker.enqueuePeriodically()
		LocationCleanupWorker.enqueuePeriodically()
		
		if (BuildConfig.USE_STETHO) {
			Stetho.initialize(Stetho.newInitializerBuilder(this)
					.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
					.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
					.build())
		}
	}
	
}