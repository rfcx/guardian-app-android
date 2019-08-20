package org.rfcx.ranger.view.login


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserAuthResponse
import org.rfcx.ranger.repo.api.UserTouchApi
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier

class LoginViewModel(private val context: Context) : ViewModel() {
	
	private val auth0 by lazy {
		val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	private var _loginState: MutableLiveData<LoginState> = MutableLiveData()
	val loginState: LiveData<LoginState>
		get() = _loginState
	
	private var _loginResult: MutableLiveData<UserAuthResponse> = MutableLiveData()
	val loginResult: LiveData<UserAuthResponse>
		get() = _loginResult
	
	private var _loginError: MutableLiveData<String?> = MutableLiveData()
	val loginError: LiveData<String?>
		get() = _loginError
	
	private var _userTouchState: MutableLiveData<UserTouchState> = MutableLiveData()
	val userTouchState: LiveData<UserTouchState>
		get() = _userTouchState
	
	private var _gotoPage: MutableLiveData<String> = MutableLiveData()
	val gotoPage: LiveData<String>
		get() = _gotoPage
	
	fun doLogin(email: String, password: String) {
		authentication
				.login(email, password, "Username-Password-Authentication")
				.setScope(context.getString(R.string.auth0_scopes))
				.setAudience(context.getString(R.string.auth0_audience))
				.start(object : BaseCallback<Credentials, AuthenticationException> {
					override fun onSuccess(credentials: Credentials) {
						val result = CredentialVerifier(context).verify(credentials)
						when (result) {
							is Err -> {
								_loginState.postValue(LoginState.FAILED)
								_loginError.postValue(result.error)
							}
							is Ok -> {
								Log.d("login1", "Ok work")
								_loginState.postValue(LoginState.SUCCESS)
								_loginResult.postValue(result.value)
							}
						}
					}
					
					override fun onFailure(exception: AuthenticationException) {
						exception.printStackTrace()
						Crashlytics.logException(exception)
						if (exception.code == "invalid_grant") {
							_loginState.postValue(LoginState.FAILED)
							_loginError.postValue(context.getString(R.string.incorrect_username_password))
						} else {
							_loginState.postValue(LoginState.FAILED)
							_loginError.postValue(exception.description)
						}
					}
				})
	}
	
	fun setLoginState() {
		_loginState.value = LoginState.NONE
	}
	
	fun loginSuccess(userAuthResponse: UserAuthResponse) {
		CredentialKeeper(context).save(userAuthResponse)
		
		UserTouchApi().send(context, object : UserTouchApi.UserTouchCallback {
			override fun onSuccess() {
				if (userAuthResponse.isRanger) {
					_userTouchState.postValue(UserTouchState.SUCCESS)
					_gotoPage.postValue("MainActivityNew")
				} else {
					_userTouchState.postValue(UserTouchState.SUCCESS)
					_gotoPage.postValue("InvitationCodeFragment")
				}
				
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				_userTouchState.postValue(UserTouchState.FAILED)
				Crashlytics.logException(t)
				_loginError.postValue(message ?: t?.localizedMessage)
			}
		})
	}
}

enum class LoginState {
	NONE, FAILED, SUCCESS
}

enum class UserTouchState {
	FAILED, SUCCESS
}

