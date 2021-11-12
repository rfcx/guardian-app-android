package org.rfcx.incidents

import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.mapbox.android.core.permissions.PermissionsManager
import io.realm.Realm
import io.realm.exceptions.RealmMigrationNeededException
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.rfcx.incidents.di.DataModule
import org.rfcx.incidents.di.UiModule
import org.rfcx.incidents.service.AirplaneModeReceiver
import org.rfcx.incidents.service.NetworkState
import org.rfcx.incidents.service.ReportCleanupWorker
import org.rfcx.incidents.util.RealmHelper
import org.rfcx.incidents.util.removeLocationUpdates
import org.rfcx.incidents.util.startLocationChange


class RangerApplication : MultiDexApplication(), LifecycleObserver {
	
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
		Realm.init(this)
		JodaTimeAndroid.init(this)
		
		setUpRealm()
		setupKoin()
		ReportCleanupWorker.enqueuePeriodically()
		registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
		
		if (BuildConfig.USE_STETHO) {
			Stetho.initialize(Stetho.newInitializerBuilder(this)
					.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
					.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
					.build())
		}
	}
	
	@OnLifecycleEvent(Lifecycle.Event.ON_START)
	fun onAppInForeground() {
		if (PermissionsManager.areLocationPermissionsGranted(this)) {
			this.startLocationChange()
		}
	}
	
	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	fun onAppInBackground() {
		this.removeLocationUpdates()
		unregisterReceiver(airplaneModeReceiver)
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
			} catch (e: RealmMigrationNeededException) {
			}
		}
	}
	
	
	private val listModules: ArrayList<Module> by lazy {
		arrayListOf(
				UiModule.mainModule,
				UiModule.eventsModule,
				UiModule.reportsModule,
				UiModule.mapModule,
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
