package org.rfcx.ranger.view.profile.editprofile

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
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
		val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
		return MultipartBody.Part.createFormData("file", file.name, requestFile)
	}
	
	private fun checkSizeImage(context: Context?, file: File): File {
		val compressed = Compressor(context)
				.setQuality(75)
				.compressToBitmap(file)
		val excessHeight = compressed.height - 2000
		val excessWidth = compressed.width - 2000
		
		if (compressed.width > 2000 && compressed.height > 2000) {
			when ((excessWidth).compareTo(excessHeight)) {
				-1 -> { // less than
					return bitmapToFile(resizeBitmap(compressed, compressed.width - excessHeight, compressed.height - excessHeight))
				}
				0 -> {  // equals
					return bitmapToFile(resizeBitmap(compressed, compressed.width - excessWidth, compressed.height - excessHeight))
				}
				1 -> { // more than
					return bitmapToFile(resizeBitmap(compressed, compressed.width - excessWidth, compressed.height - excessWidth))
				}
			}
		} else if (compressed.width > 2000) {
			return bitmapToFile(resizeBitmap(compressed, compressed.width - excessWidth, compressed.height - excessWidth))
		} else if (compressed.height > 2000) {
			return bitmapToFile(resizeBitmap(compressed, compressed.width - excessHeight, compressed.height - excessHeight))
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
	
	private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
		return Bitmap.createScaledBitmap(bitmap, width, height, false)
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