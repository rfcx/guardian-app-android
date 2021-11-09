package org.rfcx.incidents.view.profile.editprofile

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.zelory.compressor.Compressor
import io.reactivex.observers.DisposableSingleObserver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoUseCase
import org.rfcx.incidents.entity.ProfilePhotoResponse
import org.rfcx.incidents.util.getResultError
import org.rfcx.incidents.util.updateUserProfile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class EditProfileViewModel(private val context: Context, private val profilePhotoUseCase: ProfilePhotoUseCase) : ViewModel() {
	
	private val _status = MutableLiveData<Result<String>>()
	val status: LiveData<Result<String>> get() = _status
	
	fun updateProfilePhoto(path: String) {
		_status.value = Result.Loading
		
		val imageFile = File(path)
		val compressedFile = checkSizeImage(context, imageFile)
		
		profilePhotoUseCase.execute(object : DisposableSingleObserver<ProfilePhotoResponse>() {
			override fun onSuccess(t: ProfilePhotoResponse) {
				context.updateUserProfile(path)
				_status.value = Result.Success(t.url)
				
			}
			
			override fun onError(e: Throwable) {
				_status.value = e.getResultError()
			}
			
		}, createLocalFilePart(compressedFile))
	}
	
	private fun createLocalFilePart(file: File): MultipartBody.Part {
		val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
		return MultipartBody.Part.createFormData("file", file.name, requestFile)
	}
	
	private fun checkSizeImage(context: Context?, file: File): File {
		val compressed = Compressor(context)
				.setQuality(75)
				.compressToBitmap(file)
		val imageHeight = compressed.height
		val imageWidth = compressed.width
		var newWidth = 0
		var newHeight = 0
		
		if (imageHeight > 2000 && imageWidth > 2000) {
			when ((imageWidth).compareTo(imageHeight)) {
				-1 -> { // less than
					newWidth = (imageWidth * 2000) / imageHeight
					newHeight = 2000
				}
				0 -> {  // equals
					newWidth = 2000
					newHeight = 2000
				}
				1 -> { // more than
					newWidth = 2000
					newHeight = (imageHeight * 2000) / imageWidth
				}
			}
			return bitmapToFile(Bitmap.createScaledBitmap(compressed, newWidth, newHeight, false))
		} else if (compressed.width > 2000) {
			newHeight = (imageHeight * 2000) / imageWidth
			return bitmapToFile(Bitmap.createScaledBitmap(compressed, 2000, newHeight, false))
		} else if (compressed.height > 2000) {
			newWidth = (imageWidth * 2000) / imageHeight
			return bitmapToFile(Bitmap.createScaledBitmap(compressed, newWidth, 2000, false))
		}
		
		val compressedFile = Compressor(context)
				.setQuality(75)
				.compressToFile(file)
		if (compressedFile.length() > 1_000_000) {
			return compressFile(context, compressedFile)
		}
		return compressedFile
	}
	
	// Method to save an bitmap to a file
	private fun bitmapToFile(bitmap: Bitmap): File {
		// Get the context wrapper
		val wrapper = ContextWrapper(context)
		
		// Initialize a new file instance to save bitmap object
		var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
		file = File(file, "${UUID.randomUUID()}.jpg")
		
		try {
			// Compress the bitmap and save in jpg format
			val stream: OutputStream = FileOutputStream(file)
			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
			stream.flush()
			stream.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
		
		if (file.length() > 2_000_000) {
			return compressFile(context, file)
		}
		
		return file
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
