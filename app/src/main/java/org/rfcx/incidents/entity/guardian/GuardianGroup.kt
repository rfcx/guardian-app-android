package org.rfcx.incidents.entity.guardian

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * A group of guardians for subscribing notifications
 */

open class GuardianGroup : RealmObject() {
	@PrimaryKey
	var shortname: String = ""
	var name: String = ""
	var description: String = ""
	var guardians: RealmList<Guardian>? = null
	@SerializedName("site")
	var siteId: String = ""
	@SerializedName("event_values")
	var values: RealmList<String> = RealmList()
		get() = if (field.isEmpty()) RealmList("chainsaw", "vehicle") else field
	
	companion object {
		val noneGuardianGroup =
				GuardianGroup().apply {
					name = noneGuardianGroupName
					shortname = noneGuardianGroupName
					siteId = noneGuardianGroupSiteId
				}
		
		private const val noneGuardianGroupSiteId = "None"
		const val noneGuardianGroupName = "None"
	}
}