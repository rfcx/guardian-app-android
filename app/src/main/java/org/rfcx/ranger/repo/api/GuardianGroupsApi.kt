package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Retrieve the list of guardian groups from the web service
 */

class GuardianGroupsApi {

    fun getAll(context: Context, callback: OnGuardianGroupsCallback) {
        val token = context.getTokenID()
        if (token == null) {
            callback.onFailed(TokenExpireException(context), null)
            return
        }

        ApiManager.getInstance().apiRest.guardianGroups("Bearer $token")
                .enqueue(object : Callback<List<GuardianGroup>> {
                    override fun onFailure(call: Call<List<GuardianGroup>>, t: Throwable) {
                        Crashlytics.logException(t)
                        callback.onFailed(t, null)
                    }
                    override fun onResponse(call: Call<List<GuardianGroup>>, response: Response<List<GuardianGroup>>) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                callback.onSuccess(result.value)
                            }
                            is Err -> {
                                responseErrorHandler(result.error, callback, context, "GuardianGroupsApi")
                            }
                        }
                    }
                })
    }

    interface OnGuardianGroupsCallback: ApiCallback {
        fun onSuccess(groups: List<GuardianGroup>)
    }
}