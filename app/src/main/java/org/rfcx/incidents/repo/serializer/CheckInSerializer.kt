package org.rfcx.incidents.repo.serializer

import com.google.gson.*
import org.rfcx.incidents.entity.location.CheckIn
import java.lang.reflect.Type

class CheckInSerializer : JsonSerializer<CheckIn?> {
	override fun serialize(src: CheckIn?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
		val jObj = GsonBuilder().create().toJsonTree(src) as JsonObject
		jObj.remove("id")
		jObj.remove("timestamp")
		jObj.remove("synced")
		return jObj
	}
}
