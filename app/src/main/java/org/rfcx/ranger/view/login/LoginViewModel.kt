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
import com.auth0.android.callback.AuthenticationCallback
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.usertouch.CheckUserTouchUseCase
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserAuthResponse
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.CredentialVerifier
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.view.login.LoginFragment.Companion.SUCCESS

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
		
	private var _resetPassword: MutableLiveData<String?> = MutableLiveData()
	val resetPassword: LiveData<String?>
		get() = _resetPassword
	
	private var _statusUserTouch: MutableLiveData<Boolean> = MutableLiveData()
	val statusUserTouch: LiveData<Boolean>
		get() = _statusUserTouch
	
	init {
		_userAuth.postValue(null)
		_loginFailure.postValue(null)
		_statusUserTouch.postValue(null)
	}
	
	fun resetPassword(email: String) {
		authentication
				.resetPassword(email, "Username-Password-Authentication")
				.start(object : AuthenticationCallback<Void> {
					override fun onSuccess(payload: Void?) {
						_resetPassword.postValue(SUCCESS)
					}
					
					override fun onFailure(error: AuthenticationException?) {
						_resetPassword.postValue(error?.message)
					}
				})
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
								
								val preferences = Preferences.getInstance(context)
								preferences.putString(Preferences.LOGIN_WITH, "email")
							}
						}
					}
					
					override fun onFailure(exception: AuthenticationException) {
						exception.printStackTrace()
						FirebaseCrashlytics.getInstance().log(exception.message.toString())
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
						FirebaseCrashlytics.getInstance().log(exception.message.toString())
						_loginFailure.postValue(exception.localizedMessage)
					}
					
					override fun onSuccess(credentials: Credentials) {
						when (val result = CredentialVerifier(context).verify(credentials)) {
							is Err -> {
								_loginFailure.postValue(result.error)
							}
							is Ok -> {
								_userAuth.postValue(result.value)
								
								val preferences = Preferences.getInstance(context)
								preferences.putString(Preferences.LOGIN_WITH, "facebook")
							}
						}
					}
				})
	}
	
	fun loginMagicLink(activity: Activity) {
		webAuthentication
				.withConnection("") // Don't need send anything in withConnection
				.withScope(context.getString(R.string.auth0_scopes))
				.withScheme(context.getString(R.string.auth0_scheme))
				.withAudience(context.getString(R.string.auth0_audience))
				.start(activity, object : AuthCallback {
					override fun onFailure(dialog: Dialog) {
						_loginFailure.postValue("")
					}
					
					override fun onFailure(exception: AuthenticationException) {
						_loginFailure.postValue(exception.localizedMessage)
						FirebaseCrashlytics.getInstance().log(exception.message.toString())
					}
					
					override fun onSuccess(credentials: Credentials) {
						when (val result = CredentialVerifier(context).verify(credentials)) {
							is Err -> {
								_loginFailure.postValue(result.error)
							}
							is Ok -> {
								_userAuth.postValue(result.value)
								
								val preferences = Preferences.getInstance(context)
								preferences.putString(Preferences.LOGIN_WITH, "phone_number")
							}
						}
					}
				})
	}
	
	fun checkUserDetail(userAuthResponse: UserAuthResponse) {
		CredentialKeeper(context).save(userAuthResponse)
		
		checkUserTouchUseCase.execute(object : DisposableSingleObserver<Boolean>() {
			override fun onSuccess(t: Boolean) {
				_statusUserTouch.postValue(true)
			}
			
			override fun onError(e: Throwable) {
				FirebaseCrashlytics.getInstance().log(e.message.toString())
				_loginFailure.postValue(e.localizedMessage)
			}
		}, null)
	}
}

