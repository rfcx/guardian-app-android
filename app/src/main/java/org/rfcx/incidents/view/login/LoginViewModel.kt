package org.rfcx.incidents.view.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.AuthenticationCallback
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.CredentialKeeper
import org.rfcx.incidents.data.remote.common.CredentialVerifier
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.CheckUserTouchUseCase
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.entity.common.Err
import org.rfcx.incidents.entity.common.Ok
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.user.UserAuthResponse
import org.rfcx.incidents.util.CloudMessaging
import org.rfcx.incidents.view.login.LoginFragment.Companion.SUCCESS

class LoginViewModel(
    private val context: Context,
    private val checkUserTouchUseCase: CheckUserTouchUseCase,
    private val getProjectsUseCase: GetProjectsUseCase
) : ViewModel() {

    private val auth0 by lazy {
        val auth0 = Auth0(context.getString(R.string.auth0_client_id), context.getString(R.string.auth0_domain))
        // auth0.isLoggingEnabled = true
        auth0.isOIDCConformant = true
        auth0
    }

    private val authentication by lazy {
        AuthenticationAPIClient(auth0)
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

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    init {
        _userAuth.postValue(null)
        _loginFailure.postValue(null)
        _statusUserTouch.postValue(null)
    }

    fun fetchProjects() {
        getProjectsUseCase.execute(
            object : DisposableSingleObserver<List<Project>>() {
                override fun onSuccess(t: List<Project>) {
                    _projects.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _projects.value = Result.Error(e)
                }
            },
            GetProjectsParams()
        )
    }

    fun subscribe(project: Project, callback: (Boolean) -> Unit) {
        CloudMessaging.subscribeIfRequired(project.id) { status -> callback(status) }
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

    fun checkUserDetail(userAuthResponse: UserAuthResponse) {
        CredentialKeeper(context).save(userAuthResponse)

        checkUserTouchUseCase.execute(
            object : DisposableSingleObserver<Boolean>() {
                override fun onSuccess(t: Boolean) {
                    _statusUserTouch.postValue(true)
                }

                override fun onError(e: Throwable) {
                    FirebaseCrashlytics.getInstance().log(e.message.toString())
                    _loginFailure.postValue(e.localizedMessage)
                }
            },
            null
        )
    }
}
