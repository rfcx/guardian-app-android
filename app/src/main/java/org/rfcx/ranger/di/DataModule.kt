package org.rfcx.ranger.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.Preferences

object DataModule {
	
	val localModule = module {
		factory { LocationDb() }
		factory { ReportDb() }
		factory { ReportImageDb() }
		factory { Preferences.getInstance(androidContext()) }
	}
}