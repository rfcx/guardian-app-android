package org.rfcx.incidents.data.remote.project

import org.rfcx.incidents.entity.stream.Permissions
import org.rfcx.incidents.entity.stream.Project

data class ProjectResponse(
    var id: String = "",
    var name: String = "",
    var permissions: List<String> = listOf(),
    var offTimes: String = ""
)

fun ProjectResponse.permissionsLabel(): String {
    return if (this.permissions.contains("C") && this.permissions.contains("R") && this.permissions.contains("U") && this.permissions.contains(
            "D"
        )
    ) {
        Permissions.ADMIN.value
    } else if (this.permissions.contains("C") && this.permissions.contains("R") && this.permissions.contains("U")) {
        Permissions.MEMBER.value
    } else {
        Permissions.GUEST.value
    }
}

fun ProjectResponse.toProject(): Project {
    return Project(
        id = this.id,
        name = this.name,
        permissions = this.permissionsLabel(),
        offTimes = this.offTimes
    )
}
