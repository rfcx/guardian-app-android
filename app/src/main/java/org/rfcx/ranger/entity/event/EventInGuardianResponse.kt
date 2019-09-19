package org.rfcx.ranger.entity.event

import com.google.gson.annotations.SerializedName

class EventInGuardianResponse {
	@SerializedName("events")
	var events: List<Event>? = null
}