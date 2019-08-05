package org.rfcx.ranger.di

import org.koin.dsl.module
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb

object DataModule {
	
	val localModule = module {
		factory { LocationDb() }
		factory { ReportDb() }
	}
}