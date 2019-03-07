package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.guardian.Site
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Retrieve a site or a list of sites
 */

class SitesApi {

    fun getAll(context: Context, callback: OnSitesCallback) {
        val token = context.getTokenID()
        if (token == null) {
            callback.onFailed(TokenExpireException(context), null)
            return
        }

        ApiManager.getInstance().apiRest.sites("Bearer $token")
                .enqueue(object : Callback<List<Site>> {
                    override fun onFailure(call: Call<List<Site>>, t: Throwable) {
                        Crashlytics.logException(t)
                        callback.onFailed(t, null)
                    }
                    override fun onResponse(call: Call<List<Site>>, response: Response<List<Site>>) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                callback.onSuccess(result.value)
                            }
                            is Err -> {
                                responseErrorHandler(result.error, callback, context, "SitesApi")
                            }
                        }
                    }
                })
    }

    fun get(id: String, context: Context, callback: OnSiteCallback) {
        val token = context.getTokenID()
        if (token == null) {
            callback.onFailed(TokenExpireException(context), null)
            return
        }

        ApiManager.getInstance().apiRest.site("Bearer $token", id)
                .enqueue(object : Callback<Site> {
                    override fun onFailure(call: Call<Site>, t: Throwable) {
                        Crashlytics.logException(t)
                        callback.onFailed(t, null)
                    }
                    override fun onResponse(call: Call<Site>, response: Response<Site>) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                callback.onSuccess(result.value)
                            }
                            is Err -> {
                                responseErrorHandler(result.error, callback, context, "SitesApi")
                            }
                        }
                    }
                })
    }

    interface OnSitesCallback: ApiCallback {
        fun onSuccess(sites: List<Site>)
    }

    interface OnSiteCallback: ApiCallback {
        fun onSuccess(site: Site)
    }
}