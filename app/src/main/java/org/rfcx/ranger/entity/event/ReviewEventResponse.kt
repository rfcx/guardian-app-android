package org.rfcx.ranger.entity.event

import com.google.gson.annotations.SerializedName

data class ReviewEventResponse(
		@SerializedName("guid")
		val guid: String,
		@SerializedName("reviewer_confirmed")
		val reviewerConfirmed: Boolean,
		@SerializedName("reviewer_guid")
		val reviewerGuId: String,
		@SerializedName("reviewer_firstname")
		val reviewerFirstName: String,
		@SerializedName("reviewer_lastname")
		val reviewerLastName: String,
		@SerializedName("reviewer_email")
		val reviewerEmail: String
)
