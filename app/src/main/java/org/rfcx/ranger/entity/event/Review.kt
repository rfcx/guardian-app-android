package org.rfcx.ranger.entity.event

import com.google.gson.annotations.SerializedName
import java.util.*

open class Review {
	
	@SerializedName("created")
	var created: Date = Date()
	@SerializedName("confirmed")
	var confirmed: Boolean? = null
	
}

open class Reviewer {
	@SerializedName("firstname")
	var firstName: String? = ""
	
	@SerializedName("lastname")
	var lastName: String? = ""
	
	var email: String = ""
	
}