package org.rfcx.incidents.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

enum class NetworkState { ONLINE, OFFLINE }

class NetworkReceiver(private val listener: NetworkStateLister? = null) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent?.action == MainActivity.CONNECTIVITY_ACTION) {
//            this.listener?.onNetworkStateChange(if (context.isNetworkAvailable()) {
//                NetworkState.ONLINE
//            } else {
//                NetworkState.OFFLINE
//            })
//        }
    }

    interface NetworkStateLister {
        fun onNetworkStateChange(state: NetworkState)
    }
}

