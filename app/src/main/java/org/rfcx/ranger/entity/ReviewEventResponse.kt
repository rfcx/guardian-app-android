package org.rfcx.ranger.entity

/**
 * Created by Jingjoeh on 11/6/2017 AD.
 */

data class ReviewEventResponse(
        val guid: String,
        val reviewer_confirmed: Boolean,
        val reviewer_guid: String,
        val reviewer_firstname: String,
        val reviewer_lastname: String,
        val reviewer_email: String
)
