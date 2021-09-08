package org.rfcx.ranger.entity.project

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Project(
		@PrimaryKey
		var id: Int = 0,
		var name: String = "",
		var serverId: String? = null,
		var permissions: String = ""
) : RealmModel {
	companion object {
		const val TABLE_NAME = "Project"
		const val PROJECT_ID = "id"
		const val PROJECT_NAME = "name"
		const val PROJECT_SERVER_ID = "serverId"
		const val PROJECT_PERMISSIONS = "permissions"
	}
}

fun Project.isGuest(): Boolean {
	return this.permissions == Permissions.GUEST.value
}
