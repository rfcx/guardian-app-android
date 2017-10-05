package android.rfcx.org.ranger.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class GsonProvider {

     var gson: Gson = GsonBuilder()
            .create()

    companion object {
        @Volatile private var INSTANCE: GsonProvider? = null
        fun getInstance(): GsonProvider =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: GsonProvider()
                }
    }
}