package org.rfcx.ranger.view.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.rfcx.ranger.R
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getUserId
import org.rfcx.ranger.util.getUserNickname
import java.io.File
import java.sql.Timestamp


class FeedbackViewModel(private val context: Context) : ViewModel() {
	
	val db = FirebaseFirestore.getInstance()
	
	private val storage = FirebaseStorage.getInstance()
	private val storageRef = storage.reference
	private val pathImages = mutableListOf<String>()
	
	private var _statusToSaveData: MutableLiveData<String> = MutableLiveData()
	val statusToSaveData: LiveData<String>
		get() = _statusToSaveData
	
	fun saveDataInFirestore(uris: List<String>?, input: String, contextView: View) {
		val docData = hashMapOf(
				"userId" to context.getUserId(),
				"from" to from(),
				"inputFeedback" to input,
				"pathImages" to pathImages,
				"timeStamp" to Timestamp(System.currentTimeMillis()).toString()
		)
		
		db.collection("feedback")
				.add(docData)
				.addOnSuccessListener { documentReference ->
					Log.d("documentReference", documentReference.toString())
					
					if (uris != null) uploadFile(uris, documentReference)
					_statusToSaveData.postValue("Success")
					
				}
				.addOnFailureListener { e ->
					Log.d("error", e.message)
					
					Snackbar.make(contextView, R.string.feedback_submission_failed, Snackbar.LENGTH_LONG)
							.setAction(R.string.snackbar_retry) {
								saveDataInFirestore(uris, input, contextView)
							}.show()
					
					_statusToSaveData.postValue("Fail")
				}
	}
	
	private fun uploadFile(uris: List<String>, documentReference: DocumentReference) {
		var counter = 0
		
		uris.forEach {
			val file = Uri.fromFile(File(it))
			
			val ref =
					file.lastPathSegment?.let {
						storageRef.child(file.lastPathSegment!!)
					}
			val uploadTask = ref?.putFile(file)
			
			uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
				if (!task.isSuccessful) {
					task.exception?.let {
						throw it
					}
				}
				return@Continuation ref.downloadUrl
			})?.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					counter += 1
					val downloadUri = task.result
					pathImages.add(downloadUri.toString())
					Log.d("downloadUri", downloadUri.toString())
					if (counter == uris.size) {
						val docData = hashMapOf("pathImages" to pathImages)
						documentReference.update(docData as Map<String, Any>)
					}
				} else {
//					Toast.makeText(context, "Upload Image Fail", Toast.LENGTH_SHORT).show()
				}
			}
		}
	}
	
	fun from(): String {
		val preferences = Preferences.getInstance(context)
		val email = preferences.getString(Preferences.EMAIL)
		return email ?: context.getUserNickname()
	}
	
	companion object {
		const val tag = "FeedbackViewModel"
	}
	
}