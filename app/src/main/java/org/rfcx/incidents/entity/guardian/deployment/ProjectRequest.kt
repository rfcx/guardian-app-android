package org.rfcx.incidents.entity.guardian.deployment

import org.rfcx.incidents.entity.stream.Project

data class ProjectRequest(
    var name: String? = null,
    var id: String? = null
)
