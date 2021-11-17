package org.rfcx.incidents.entity.guardian

import com.google.gson.annotations.SerializedName

data class GroupByGuardiansResponse(
		@SerializedName("guardians")
		val guardians: List<Guardian>
)
