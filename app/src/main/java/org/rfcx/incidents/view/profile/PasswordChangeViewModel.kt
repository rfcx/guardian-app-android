package org.rfcx.incidents.view.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.data.remote.password.PasswordChangeUseCase
import org.rfcx.incidents.entity.PasswordRequest
import org.rfcx.incidents.entity.PasswordResponse

class PasswordChangeViewModel(private val passwordChangeUseCase: PasswordChangeUseCase) : ViewModel() {

    private val _status = MutableLiveData<Result<String>>()
    val status: LiveData<Result<String>> get() = _status

    fun changeUserPassword(newPassword: String) {
        Log.d("passwordChangeUseCase", "newPassword $newPassword")
        _status.value = Result.Loading
        passwordChangeUseCase.execute(
            object : DisposableSingleObserver<PasswordResponse>() {
                override fun onSuccess(t: PasswordResponse) {
                    Log.d("passwordChangeUseCase", "onSuccess ${t.success}")
                    _status.value = Result.Success(t.success.toString())
                }

                override fun onError(e: Throwable) {
                    Log.d("passwordChangeUseCase", "onError $e")
                    _status.value = Result.Error(e)
                }
            },
            PasswordRequest(newPassword)
        )
    }
}
