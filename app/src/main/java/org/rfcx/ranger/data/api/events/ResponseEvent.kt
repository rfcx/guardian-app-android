package org.rfcx.ranger.data.api.events

import org.rfcx.ranger.entity.Classification
import org.rfcx.ranger.entity.Incident
import org.rfcx.ranger.entity.alert.Alert
import java.util.*

data class ResponseEvent(
		var id: String = "",
		var name: String = "",
		var streamId: String = "",
		var projectId: String = "",
		var createdAt: Date = Date(),
		var start: Date = Date(),
		var end: Date = Date(),
		var classification: ClassificationRequest = ClassificationRequest(),
		var incident: IncidentRequest = IncidentRequest()
)

data class ClassificationRequest(
		var value: String = "",
		var title: String = ""
)

data class IncidentRequest(
		var id: String = "",
		var closedAt: Date? = null,
		var createdAt: Date = Date()
)

fun ResponseEvent.toAlert(): Alert = Alert(
		serverId = id,
		name = name,
		streamId = streamId,
		projectId = projectId,
		createdAt = createdAt,
		start = start,
		end = end,
		classification = classification.toIncident(),
		incident = incident.toIncident()
)

fun ClassificationRequest.toIncident(): Classification = Classification(
		value = value,
		title = title
)

fun IncidentRequest.toIncident(): Incident = Incident(
		id = id,
		closedAt = closedAt,
		createdAt = createdAt
)
