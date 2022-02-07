package org.rfcx.incidents.data.remote.common.service

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import org.rfcx.incidents.util.getTokenID

class AuthTokenInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val oldRequest = chain.request()
        val request = oldRequest.newBuilder()
            .addHeader("Authorization", "Bearer " + context.getTokenID())
            .build()
        return chain.proceed(request)
    }
}
