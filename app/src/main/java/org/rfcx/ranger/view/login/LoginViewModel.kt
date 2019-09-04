package org.rfcx.ranger.view.login


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
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
	
	private var _userAuth: MutableLiveData<UserAuthResponse?> = MutableLiveData()
	val userAuth: LiveData<UserAuthResponse?>
		get() = _userAuth
	
	private var _loginFailure: MutableLiveData<String?> = MutableLiveData()
	val loginFailure: LiveData<String?>
		get() = _loginFailure
	
	private var _redirectPage: MutableLiveData<LoginRedirect?> = MutableLiveData()
	val redirectPage: LiveData<LoginRedirect?>
		get() = _redirectPage
	
	init {
		_userAuth.postValue(null)
		_loginFailure.postValue(null)
		_redirectPage.postValue(null)
	}
	
	fun login(email: String, password: String) {
		authentication
				.login(email, password, "Username-Password-Authentication")
				.setScope(context.getString(R.string.auth0_scopes))
				.setAudience(context.getString(R.string.auth0_audience))
				.start(object : BaseCallback<Credentials, AuthenticationException> {
					override fun onSuccess(credentials: Credentials) {
						when (val result = CredentialVerifier(context).verify(credentials)) {
							is Err -> {
								_loginFailure.postValue(result.error)
							}
							is Ok -> {
								_userAuth.postValue(result.value)
							}
						}
					}
					
					override fun onFailure(exception: AuthenticationException) {
						exception.printStackTrace()
						Crashlytics.logException(exception)
						if (exception.code == "invalid_grant") {
							_loginFailure.postValue(context.getString(R.string.incorrect_username_password))
						} else {
							_loginFailure.postValue(exception.description)
						}
					}
				})
	}
	
	fun loginWithFacebook(activity: Activity) {
		webAuthentication
				.withConnection("facebook")
				.withScope(context.getString(R.string.auth0_scopes))
				.withScheme(context.getString(R.string.auth0_scheme))
				.withAudience(context.getString(R.string.auth0_audience))
				.start(activity, object : AuthCallback {
					override fun onFailure(dialog: Dialog) {
						_loginFailure.postValue("")
					}
					
					override fun onFailure(exception: AuthenticationException) {
						Crashlytics.logException(exception)
						_loginFailure.postValue(exception.localizedMessage)
					}
					
					override fun onSuccess(credentials: Credentials) {
						when (val result = CredentialVerifier(context).verify(credentials)) {
							is Err -> {
								_loginFailure.postValue(result.error)
							}
							is Ok -> {
								_userAuth.postValue(result.value)
							}
						}
					}
				})
	}
	
	fun loginMagicLink(activity: Activity) {
		webAuthentication
				.withScope(context.getString(R.string.auth0_scopes))
				.withScheme(context.getString(R.string.auth0_scheme))
				.withAudience(context.getString(R.string.auth0_audience))
				.start(activity, object : AuthCallback {
					override fun onFailure(dialog: Dialog) {
						_loginFailure.postValue("")
						Log.d("MagicLink onFailure", dialog.toString())
					}
					
					override fun onFailure(exception: AuthenticationException) {
						Log.d("MagicLink onFailure", exception.toString())
						_loginFailure.postValue(exception.localizedMessage)
					}
					
					override fun onSuccess(credentials: Credentials) {
						Log.d("MagicLink onSuccess", credentials.toString())
						when (val result = CredentialVerifier(context).verify(credentials)) {
							is Err -> {
								_loginFailure.postValue(result.error)
							}
							is Ok -> {
								_userAuth.postValue(result.value)
							}
						}
					}
				})
	}

//	fun setLoginState() {
//		_loginState.value = LoginState.NONE
//		_userTouchState.value = UserTouchState.NONE
//	}

	fun checkUserDetail(userAuthResponse: UserAuthResponse) {
		CredentialKeeper(context).save(userAuthResponse)
		
		checkUserTouchUseCase.execute(object : DisposableSingleObserver<Boolean>() {
			override fun onSuccess(t: Boolean) {
				if (userAuthResponse.isRanger) {
					_redirectPage.postValue(LoginRedirect.MAIN_PAGE)
				} else {
					_redirectPage.postValue(LoginRedirect.INVITE_CODE_PAGE)
				}
			}
			
			override fun onError(e: Throwable) {
				Crashlytics.logException(e)
				_loginFailure.postValue(e.localizedMessage)
			}
		}, null)
	}
}

enum class LoginRedirect {
	MAIN_PAGE, INVITE_CODE_PAGE
}