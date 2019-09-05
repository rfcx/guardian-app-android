package org.rfcx.ranger.entity.event

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class EventReview(
		@PrimaryKey
		var eventGuId: String = "",
		
		// state of ReviewEventFactory.confirm,  ReviewEventFactory.reject, null is not review
		var review: String? = null
) : RealmModel