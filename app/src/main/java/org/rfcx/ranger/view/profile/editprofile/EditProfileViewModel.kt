package org.rfcx.ranger.view.profile.editprofile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import io.reactivex.observers.DisposableSingleObserver
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.profilephoto.ProfilePhotoUseCase
import org.rfcx.ranger.entity.ProfilePhotoResponse
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.updateUserProfile
import org.rfcx.ranger.view.alerts.EventGroup
import java.io.File

class EditProfileViewModel(private val context: Context, private val profilePhotoUseCase: ProfilePhotoUseCase) : ViewModel() {
	
	private val _status = MutableLiveData<Result<String>>()
	val status: LiveData<Result<String>> get() = _status
	
	fun updateProfilePhoto(path: String) {
		_status.value = Result.Loading
		
		val imageFile = File(path)
		val compressedFile = compressFile(context, imageFile)
		
		profilePhotoUseCase.execute(object : DisposableSingleObserver<ProfilePhotoResponse>() {
			override fun onSuccess(t: ProfilePhotoResponse) {
				context.updateUserProfile(t.url)
				_status.value = Result.Success(t.url)
				
			}
			
			override fun onError(e: Throwable) {
				_status.value = e.getResultError()
			}
			
		}, createLocalFilePart(compressedFile))
	}
	
	private fun createLocalFilePart(file: File): MultipartBody.Part {
		val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
		return MultipartBody.Part.createFormData("file", file.name, requestFile)
	}
	
	private fun compressFile(context: Context?, file: File): File {
		
		if (file.length() <= 0) {
			return file
		}
		val compressed = Compressor(context)
				.setQuality(75)
				.compressToFile(file)
		if (compressed.length() > 1_000_000) {
			return compressFile(context, compressed)
		}
		return compressed
	}
}