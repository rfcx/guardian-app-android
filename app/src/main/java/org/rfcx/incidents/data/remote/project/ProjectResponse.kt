package org.rfcx.incidents.data.remote.project

import org.rfcx.incidents.entity.project.Permissions
import org.rfcx.incidents.entity.project.Project

data class ProjectResponse(
    var id: String = "",
    var name: String = "",
    var permissions: List<String> = listOf()
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
        name = this.name,
        serverId = this.id,
        permissions = this.permissionsLabel()
    )
}
