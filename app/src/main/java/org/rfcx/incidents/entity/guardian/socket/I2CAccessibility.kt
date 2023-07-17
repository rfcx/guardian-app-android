package org.rfcx.incidents.entity.guardian.socket

import com.google.gson.annotations.SerializedName

data class I2CAccessibility(
    @SerializedName("is_accessible")
    val isAccessible: Boolean = false,
    val message: String? = null
)
