package org.rfcx.ranger.data.api.events

import java.util.*

data class ResponseEvent(
		var id: String = "",
		var name: String = "",
		var streamId: String = "",
		var projectId: String = "",
		var createdAt: Date = Date(),
		var start: Date = Date(),
		var end: Date = Date(),
		var classification: Classification = Classification(),
		var incident: Incident = Incident()
)

data class Classification(
		var value: String = "",
		var title: String = ""
)

data class Incident(
		var id: String = "",
		var closedAt: Date = Date(),
		var createdAt: Date = Date()
)
