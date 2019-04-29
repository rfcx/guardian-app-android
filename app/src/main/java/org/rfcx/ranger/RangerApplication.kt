package org.rfcx.ranger

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import org.rfcx.ranger.service.LocationCleanupWorker
import org.rfcx.ranger.service.ReportCleanupWorker
import org.rfcx.ranger.util.RealmHelper

class RangerApplication : MultiDexApplication() {
	
	override fun onCreate() {
		super.onCreate()
		MultiDex.install(this)
		Realm.init(this)
		Realm.setDefaultConfiguration(RealmHelper.defaultConfig())
		val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
		val kit = Crashlytics.Builder().core(core).build()
		Fabric.with(this, kit)
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