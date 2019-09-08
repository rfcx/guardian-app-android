package org.rfcx.ranger.view.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getUserId
import java.io.File
import java.sql.Timestamp

class FeedbackViewModel(private val context: Context) : ViewModel() {
	
	val db = FirebaseFirestore.getInstance()
	
	private val storage = FirebaseStorage.getInstance()
	private val storageRef = storage.reference
	private val pathImages = mutableListOf<String>()
	
	fun uploadFile(uris: List<String>?, input: String) {
		if (uris != null) {
			val preferences = Preferences.getInstance(context)
			val email = preferences.getString(Preferences.EMAIL, preferences.getString(Preferences.USER_GUID, ""))
			var counter = 0
			
			uris.forEach {
				val file = Uri.fromFile(File(it))
				val ref = storageRef.child(file.lastPathSegment)
				val uploadTask = ref.putFile(file)
				
				uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
					if (!task.isSuccessful) {
						task.exception?.let {
							throw it
						}
					}
					return@Continuation ref.downloadUrl
				}).addOnCompleteListener { task ->
					if (task.isSuccessful) {
						counter += 1
						val downloadUri = task.result
						pathImages.add(downloadUri.toString())

						if(counter == uris.size){
							val docData = hashMapOf(
									"userId" to context.getUserId(),
									"email" to email,
									"inputFeedback" to input,
									"pathImages" to pathImages,
									"timeStamp" to Timestamp(System.currentTimeMillis()).toString()
							)
							
							db.collection("feedback")
									.add(docData)
									.addOnSuccessListener { documentReference ->
										Log.d("documentReference", documentReference.toString())
										Toast.makeText(context, "Data Stored", Toast.LENGTH_SHORT).show()
									}
									.addOnFailureListener { e ->
										Log.d("error", e.message)
										Toast.makeText(context, "Data Not Stored", Toast.LENGTH_SHORT).show()
									}
						}
					} else {
						Toast.makeText(context, "Upload Image Fail", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
	}
	
	companion object {
		const val tag = "FeedbackViewModel"
	}
	
}