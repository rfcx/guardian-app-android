package org.rfcx.incidents.view.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.setusername.SendNameUseCase
import org.rfcx.incidents.entity.Err
import org.rfcx.incidents.entity.Ok
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.CredentialVerifier
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.getUserId

class SetUserNameViewModel(private val context: Context, private val sendNameUseCase: SendNameUseCase) : ViewModel() {
    
    private var idUser: String = ""
    
    private val auth0 by lazy {
        val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
        //auth0.isLoggingEnabled = true
        auth0.isOIDCConformant = true
        auth0
    }
    
    private val authentication by lazy {
        AuthenticationAPIClient(auth0)
    }
    
    private var _status: MutableLiveData<Boolean> = MutableLiveData()
    val status: LiveData<Boolean>
        get() = _status
    
    fun sendName(name: String) {
        idUser = context.getUserId()
        sendNameUseCase.execute(object : DisposableSingleObserver<SetNameResponse>() {
            override fun onSuccess(t: SetNameResponse) {
                refreshToken { success ->
                    if (success) {
                        _status.postValue(true)
                    } else {
                        _status.postValue(false)
                    }
                }
            }
            
            override fun onError(e: Throwable) {
                _status.postValue(false)
            }
        }, SetNameRequest(idUser, name, name, name))
    }
    
    private fun refreshToken(callback: (Boolean) -> Unit) {
        val refreshToken = Preferences.getInstance(context).getString(Preferences.REFRESH_TOKEN)
        if (refreshToken == null) {
            callback(false)
            return
        }
        
        authentication.renewAuth(refreshToken).start(object : BaseCallback<Credentials, AuthenticationException> {
            override fun onSuccess(credentials: Credentials) {
                val result = CredentialVerifier(context).verify(credentials)
                when (result) {
                    is Err -> {
                        callback(false)
                    }
                    is Ok -> {
                        val userAuthResponse = result.value
                        if (userAuthResponse.isRanger) {
                            CredentialKeeper(context).save(userAuthResponse)
                        }
                        callback(userAuthResponse.isRanger)
                    }
                }
            }
            
            override fun onFailure(error: AuthenticationException) {
                callback(false)
            }
        })
    }
}
