package org.rfcx.ranger.view.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.password.PasswordChangeUseCase
import org.rfcx.ranger.entity.PasswordRequest
import org.rfcx.ranger.entity.PasswordResponse

class PasswordChangeViewModel(private val passwordChangeUseCase: PasswordChangeUseCase) : ViewModel() {
	
	private val _status = MutableLiveData<Result<String>>()
	val status: LiveData<Result<String>> get() = _status
	
	fun changeUserPassword(newPassword: String) {
		Log.d("passwordChangeUseCase", "newPassword $newPassword")
		_status.value = Result.Loading
		passwordChangeUseCase.execute(object : DisposableSingleObserver<PasswordResponse>() {
			override fun onSuccess(t: PasswordResponse) {
				Log.d("passwordChangeUseCase", "onSuccess ${t.success}")
				_status.value = Result.Success(t.success.toString())
			}
			
			override fun onError(e: Throwable) {
				Log.d("passwordChangeUseCase", "onError $e")
				_status.value = Result.Error(e)
			}
		}, PasswordRequest(newPassword))
	}
}