package org.rfcx.ranger.util

import android.location.Location
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp

object LocationServiceLogs {
	private const val db_collection_logs = "location-service-logs"
	
	fun start(db: FirebaseFirestore, from: String, callback: (Boolean, String?) -> Unit) {
		val docData = hashMapOf("from" to from,
				"start_time" to Timestamp(System.currentTimeMillis()).toString(),
				"end_time" to "")
		db.collection(db_collection_logs)
				.add(docData)
				.addOnSuccessListener { documentReference ->
					callback(true, documentReference.id)
				}
				.addOnFailureListener { e ->
					Log.e("LocationServiceLogs", "create new log: $e")
					callback(false, null)
				}
	}
	
	
	fun addLastKnowLocation(db: FirebaseFirestore, documentId:String, location: Location?) {
		val docRef = db.collection(db_collection_logs).document(documentId)
		
		val docData = hashMapOf("location" to location.getLatLng(),
				"time" to Timestamp(System.currentTimeMillis()).toString())
		
		docRef.collection("locations")
				.add(docData)
				.addOnCompleteListener {
					Log.i("LocationServiceLogs", "add location log: ${location.getLatLng()}")
				}
	}
	
	fun setEndTime(db: FirebaseFirestore, documentId: String) {
		val docRef = db.collection(db_collection_logs).document(documentId)
		docRef.update("end_time", Timestamp(System.currentTimeMillis()).toString())
	}
	
	fun Location?.getLatLng(): String {
		this ?: return "null"
		return "${this.latitude},${this.longitude}"
	}
}