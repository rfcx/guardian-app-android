package org.rfcx.incidents.entity.event

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class EventReview(
		@PrimaryKey
		var eventGuId: String = "",
		
		// state of ReviewEventFactory.confirm,  ReviewEventFactory.reject, null is not review
		var review: String? = null,
		
		var syncState: Int = 0 // 0 unsent, 1 uploading, 2 uploaded (sync complete)

) : RealmModel {
	companion object {
		const val UNSENT = 0
		const val SENDING = 1
		const val SENT = 2
	}
}
