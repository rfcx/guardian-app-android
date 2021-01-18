package org.rfcx.ranger.view.login

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
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.terms.TermsUseCase
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.terms.TermsRequest
import org.rfcx.ranger.entity.terms.TermsResponse
import org.rfcx.ranger.util.*

class TermsAndServiceViewModel(private val context: Context, private val termsUseCase: TermsUseCase) : ViewModel() {
	
	private val auth0 by lazy {
		val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	private val _consentGivenState = MutableLiveData<Result<Boolean>>()
	val consentGivenState: LiveData<Result<Boolean>> get() = _consentGivenState
	
	fun acceptTerms() {
		_consentGivenState.value = Result.Loading
		
		termsUseCase.execute(object : DisposableSingleObserver<TermsResponse>() {
			override fun onSuccess(t: TermsResponse) {
				if (t.success) {
					refreshToken {
						if (it) {
							_consentGivenState.postValue(Result.Success(true))
						} else {
							Result.Error(Throwable(context.getString(R.string.something_is_wrong)))
						}
					}
				} else {
					Result.Error(Throwable(context.getString(R.string.something_is_wrong)))
				}
			}
			
			override fun onError(e: Throwable) {
				_consentGivenState.value = Result.Error(e)
			}
		}, TermsRequest("RangerApp"))
	}
	
	private fun refreshToken(callback: (Boolean) -> Unit) {
		val refreshToken = Preferences.getInstance(context).getString(Preferences.REFRESH_TOKEN)
		if (refreshToken == null) {
			callback(false)
			return
		}
		
		authentication.renewAuth(refreshToken).start(object : BaseCallback<Credentials, AuthenticationException> {
			override fun onSuccess(credentials: Credentials) {
				when (val result = CredentialVerifier(context).verify(credentials)) {
					is Err -> {
						callback(false)
					}
					is Ok -> {
						val userAuthResponse = result.value
						if (userAuthResponse.roles.contains("rfcxUser")) {
							CredentialKeeper(context).save(userAuthResponse)
						}
						callback(userAuthResponse.roles.contains("rfcxUser"))
					}
				}
			}
			
			override fun onFailure(error: AuthenticationException) {
				callback(false)
			}
		})
	}
}
