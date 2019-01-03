package org.rfcx.ranger.entity.event

data class ReviewEventResponse(
        val guid: String,
        val reviewer_confirmed: Boolean,
        val reviewer_guid: String,
        val reviewer_firstname: String,
        val reviewer_lastname: String,
        val reviewer_email: String
)
