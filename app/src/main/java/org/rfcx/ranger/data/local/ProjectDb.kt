package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.permissionsLabel
import org.rfcx.ranger.data.api.project.toProject
import org.rfcx.ranger.entity.project.Project

class ProjectDb(val realm: Realm) {
	
	fun insertOrUpdate(response: ProjectResponse) {
		realm.executeTransaction {
			val project =
					it.where(Project::class.java)
							.equalTo(Project.PROJECT_SERVER_ID, response.id)
							.findFirst()
			
			if (project == null) {
				val projectObject = response.toProject()
				val id =
						(it.where(Project::class.java).max(Project.PROJECT_ID)
								?.toInt() ?: 0) + 1
				projectObject.id = id
				it.insert(projectObject)
			} else {
				project.serverId = response.id
				project.name = response.name
				project.permissions = response.permissionsLabel()
			}
		}
	}
}
