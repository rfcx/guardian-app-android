package org.rfcx.ranger.view.login


import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.crashlytics.android.Crashlytics
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.usertouch.CheckUserTouchUseCase
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserAuthResponse
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier

class LoginViewModel(private val context: Context, private val checkUserTouchUseCase: CheckUserTouchUseCase) : ViewModel() {
	
	private val auth0 by lazy {
		val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
		//auth0.isLoggingEnabled = true
		auth0.isOIDCConformant = true
		auth0
	}
	
	private val authentication by lazy {
		AuthenticationAPIClient(auth0)
	}
	
	private val webAuthentication by lazy {
		WebAuthProvider.init(auth0)
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
	
	fun onLoginWithFacebook(activity: Activity) {
		webAuthentication
				.withConnection("facebook")
				.withScope(context.getString(R.string.auth0_scopes))
				.withScheme(context.getString(R.string.auth0_scheme))
				.withAudience(context.getString(R.string.auth0_audience))
				.start(activity, object : AuthCallback {
					override fun onFailure(dialog: Dialog) {
						_loginState.postValue(LoginState.FAILED)
						_loginError.postValue(null)
					}
					
					override fun onFailure(exception: AuthenticationException) {
						Crashlytics.logException(exception)
						_loginState.postValue(LoginState.FAILED)
						_loginError.postValue(exception.localizedMessage)
						
					}
					
					override fun onSuccess(credentials: Credentials) {
						val result = CredentialVerifier(context).verify(credentials)
						when (result) {
							is Err -> {
								_loginState.postValue(LoginState.FAILED)
								_loginError.postValue(result.error)
							}
							is Ok -> {
								_loginState.postValue(LoginState.SUCCESS)
								_loginResult.postValue(result.value)
							}
						}
					}
				})
	}
	
	fun setLoginState() {
		_loginState.value = LoginState.NONE
		_userTouchState.value = UserTouchState.NONE
	}
	
	fun loginSuccess(userAuthResponse: UserAuthResponse) {
		CredentialKeeper(context).save(userAuthResponse)
		
		checkUserTouchUseCase.execute(object : DisposableSingleObserver<Boolean>() {
			override fun onSuccess(t: Boolean) {
				if (userAuthResponse.isRanger) {
					_userTouchState.postValue(UserTouchState.SUCCESS)
					_gotoPage.postValue("MainActivityNew")
				} else {
					_userTouchState.postValue(UserTouchState.SUCCESS)
					_gotoPage.postValue("InvitationCodeFragment")
				}
			}
			
			override fun onError(e: Throwable) {
				_userTouchState.postValue(UserTouchState.FAILED)
				Crashlytics.logException(e)
				_loginError.postValue(e.localizedMessage)
			}
		}, null)
	}
}

enum class LoginState {
	NONE, FAILED, SUCCESS
}

enum class UserTouchState {
	NONE, FAILED, SUCCESS
}

