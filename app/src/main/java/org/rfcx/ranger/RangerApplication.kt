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
		Realm.setDefaultConfiguration(defaultReamConfig())
		
		if (BuildConfig.VERSION_CODE == 12) {
			// Remove old realm on next Release.
			// TODO remove this when release a news version more than 12
			val oldRealm = RealmConfiguration.Builder()
					.name("default.realm")
					.deleteRealmIfMigrationNeeded()
					.build()
			Realm.deleteRealm(oldRealm)
		}
		
		ReportCleanupWorker.enqueuePeriodically()
		
		if (BuildConfig.USE_STETHO) {
			Stetho.initialize(Stetho.newInitializerBuilder(this)
					.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
					.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
					.build())
		}
	}
	
	private fun defaultReamConfig(): RealmConfiguration {
		return RealmConfiguration.Builder()
				.name("Ranger.Realm")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build()
	}
	
}