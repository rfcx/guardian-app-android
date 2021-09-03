package org.rfcx.ranger

import android.util.Log
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import io.realm.Realm
import io.realm.exceptions.RealmMigrationNeededException
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.rfcx.ranger.di.DataModule
import org.rfcx.ranger.di.UiModule
import org.rfcx.ranger.service.CleanupAudioCacheWorker
import org.rfcx.ranger.service.LocationCleanupWorker
import org.rfcx.ranger.service.ReportCleanupWorker
import org.rfcx.ranger.util.RealmHelper


class RangerApplication : MultiDexApplication() {
	
	override fun onCreate() {
		super.onCreate()
		
		MultiDex.install(this)
		Realm.init(this)
		JodaTimeAndroid.init(this)
		
		setUpRealm()
		setupKoin()
		ReportCleanupWorker.enqueuePeriodically()
		LocationCleanupWorker.enqueuePeriodically()
		CleanupAudioCacheWorker.enqueuePeriodically()
		
		if (BuildConfig.USE_STETHO) {
			Stetho.initialize(Stetho.newInitializerBuilder(this)
					.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
					.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
					.build())
		}
		
		
	}
	
	private fun setUpRealm() {
		var realmNeedsMigration = false
		try {
			val realm = Realm.getInstance(RealmHelper.migrationConfig())
			realm.close()
			Realm.setDefaultConfiguration(RealmHelper.migrationConfig())
		} catch (e: RealmMigrationNeededException) {
			Log.e("RealmMigration", "${e.message}")
			realmNeedsMigration = true
		}
		
		// Falback for release (delete realm on error)
		if (realmNeedsMigration && !BuildConfig.DEBUG) {
			try {
				val realm = Realm.getInstance(RealmHelper.fallbackConfig())
				realm.close()
			} catch (e: RealmMigrationNeededException) { }
		}
	}
	
	
	private val listModules: ArrayList<Module> by lazy {
		arrayListOf(
				UiModule.mainModule,
				UiModule.mapModule,
				UiModule.statusModule,
				UiModule.alertModule,
				UiModule.profileModule,
				UiModule.loginModule,
				DataModule.localModule,
				DataModule.remoteModule,
				DataModule.dataModule
		)
	}
	
	private fun setupKoin() {
		startKoin {
			androidContext(this@RangerApplication)
			modules(listModules)
		}
	}
	
}
