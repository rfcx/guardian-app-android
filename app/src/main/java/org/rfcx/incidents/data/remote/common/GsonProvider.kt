package org.rfcx.incidents.data.remote.common

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class GsonProvider {

    var gson: Gson = GsonBuilder()
        .create()

    companion object {
        @Volatile
        private var INSTANCE: GsonProvider? = null
        fun getInstance(): GsonProvider =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: GsonProvider()
            }
    }
}
