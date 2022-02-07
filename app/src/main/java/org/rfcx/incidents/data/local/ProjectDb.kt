package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.Sort
import org.rfcx.incidents.data.remote.project.ProjectResponse
import org.rfcx.incidents.data.remote.project.toProject
import org.rfcx.incidents.entity.stream.Project

class ProjectDb(val realm: Realm) {

    fun insertOrUpdate(response: ProjectResponse) {
        realm.executeTransaction {
            it.insertOrUpdate(response.toProject())
        }
    }

    fun getProject(id: String): Project? {
        return realm.where(Project::class.java).equalTo(Project.PROJECT_ID, id).findFirst()
    }

    fun getProjects(): List<Project> {
        return realm.where(Project::class.java).sort(Project.PROJECT_NAME, Sort.ASCENDING).findAll()
    }
}
