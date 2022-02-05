package org.rfcx.incidents.view

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import org.rfcx.incidents.domain.executor.PostExecutionThread

/**
 * MainThread (UI Thread) implementation based on a [Scheduler]
 * which will fatchCategory actions on the Android UI thread
 */
class UiThread : PostExecutionThread {

    override val scheduler: Scheduler
        get() = AndroidSchedulers.mainThread()
}
