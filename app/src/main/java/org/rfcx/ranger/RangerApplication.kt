package org.rfcx.ranger

import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.exceptions.RealmMigrationNeededException
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.rfcx.ranger.di.DataModule
import org.rfcx.ranger.di.UiModule
import org.rfcx.ranger.service.LocationCleanupWorker
import org.rfcx.ranger.service.ReportCleanupWorker
import org.rfcx.ranger.util.RealmHelper


class RangerApplication : MultiDexApplication() {
	
	override fun onCreate() {
		super.onCreate()
		
		MultiDex.install(this)
		Realm.init(this)
		
		val core = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
		val kit = Crashlytics.Builder().core(core).build()
		Fabric.with(this, kit)
		setUpRealm()
		setupKoin()
		ReportCleanupWorker.enqueuePeriodically()
		LocationCleanupWorker.enqueuePeriodically()
		
		if (BuildConfig.USE_STETHO) {
			Stetho.initialize(Stetho.newInitializerBuilder(this)
					.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
					.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
					.build())
		}
		
		
	}
	
	private fun setUpRealm() {
		val realm: Realm
		try {
			realm = Realm.getInstance(RealmHelper.migrationConfig())
			realm.close()
			Realm.setDefaultConfiguration(RealmHelper.migrationConfig())
		} catch (e: RealmMigrationNeededException) {
			Realm.setDefaultConfiguration(RealmHelper.defaultConfig())
		}
	}
	
	
	private val listModules: ArrayList<Module> by lazy {
		arrayListOf(
				UiModule.mapModule,
				UiModule.statusModel
				, DataModule.localModule
		)
	}
	
	private fun setupKoin() {
		startKoin {
			androidContext(this@RangerApplication)
			modules(listModules)
		}
	}
	
}