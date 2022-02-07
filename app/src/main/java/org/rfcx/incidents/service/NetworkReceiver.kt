package org.rfcx.incidents.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.rfcx.incidents.util.isNetworkAvailable

enum class NetworkState { ONLINE, OFFLINE }

class NetworkReceiver(private val listener: NetworkStateLister? = null) : BroadcastReceiver() {

    companion object {
        const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == CONNECTIVITY_ACTION) {
            this.listener?.onNetworkStateChange(
                if (context.isNetworkAvailable()) {
                    NetworkState.ONLINE
                } else {
                    NetworkState.OFFLINE
                }
            )
        }
    }

    interface NetworkStateLister {
        fun onNetworkStateChange(state: NetworkState)
    }
}
