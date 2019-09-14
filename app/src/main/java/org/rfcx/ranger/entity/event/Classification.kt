package org.rfcx.ranger.entity.event

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class ClassificationBody(
		@SerializedName("annotatorGuid")
		val annotatorGuid: String?,
		@SerializedName("annotatorType")
		val annotatorType: String = "model",
		@SerializedName("audioGuids")
		val audioGuids: String?,
		@SerializedName("type")
		val type: String = "classification",
		@SerializedName("value")
		val value: String?
)

data class ClassificationResponse(
		@SerializedName("data")
		val data: Data?,
		@SerializedName("links")
		val links: Links?
)

data class Data(
		@SerializedName("attributes")
		val attributes: Attributes?,
		@SerializedName("type")
		val type: String?
)

data class Attributes(
		@SerializedName("tags")
		val tags: JsonObject?
)

data class Confidence(
		@SerializedName("begins_at_offset")
		val beginsAtOffset: Long,
		@SerializedName("confidence")
		val confidence: Int,
		@SerializedName("ends_at_offset")
		val endsAtOffset: Long
)

data class Links(
		@SerializedName("self")
		val self: String?
)