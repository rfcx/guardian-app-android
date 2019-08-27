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
import org.rfcx.ranger.data.remote.invitecode.SendInviteCodeUseCase
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.InvitationCodeRequest
import org.rfcx.ranger.entity.user.InvitationCodeResponse
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier
import org.rfcx.ranger.util.Preferences

class InvitationCodeViewModel(private val context: Context, private val sendInviteCodeUseCase: SendInviteCodeUseCase) : ViewModel() {
	
	private val auth0 by lazy {
		val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	private var _submitCodeState: MutableLiveData<SubmitState> = MutableLiveData()
	val submitCodeState: LiveData<SubmitState>
		get() = _submitCodeState
	
	fun doSubmit(code: String) {
		submit(code) { success ->
			if (success) {
				_submitCodeState.postValue(SubmitState.SUCCESS)
			} else {
				_submitCodeState.postValue(SubmitState.FAILED)
			}
		}
	}
	
	fun setSubmitState() {
		_submitCodeState.value = SubmitState.NONE
	}
	
	private fun submit(code: String, callback: (Boolean) -> Unit) {
		sendInviteCodeUseCase.execute(object : DisposableSingleObserver<InvitationCodeResponse>() {
			override fun onSuccess(t: InvitationCodeResponse) {
				refreshToken { success ->
					callback(success)
				}
			}
			
			override fun onError(e: Throwable) {
				callback(false)
			}
		}, InvitationCodeRequest(code))
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

enum class SubmitState {
	NONE, FAILED, SUCCESS
}
