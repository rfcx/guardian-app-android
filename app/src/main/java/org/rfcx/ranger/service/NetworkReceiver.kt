package org.rfcx.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.rfcx.ranger.util.isNetWorkAvailable
import org.rfcx.ranger.view.MainActivity

enum class NetworkState { ONLINE, OFFLINE }

class NetworkReceiver(private val listener: NetworkStateLister? = null) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == MainActivity.CONNECTIVITY_ACTION) {
            this.listener?.onNetworkStateChange(if (context.isNetWorkAvailable()) {
                NetworkState.ONLINE
            } else {
                NetworkState.OFFLINE
            })
        }
    }

    interface NetworkStateLister {
        fun onNetworkStateChange(state: NetworkState)
    }
}

