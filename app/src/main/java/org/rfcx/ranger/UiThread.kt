package org.rfcx.ranger

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread

/**
 * MainThread (UI Thread) implementation based on a [Scheduler]
 * which will fatchCategory actions on the Android UI thread
 */
class UiThread() : PostExecutionThread {
	
	override val scheduler: Scheduler
		get() = AndroidSchedulers.mainThread()
	
}