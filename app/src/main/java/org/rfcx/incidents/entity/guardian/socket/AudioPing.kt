package org.rfcx.incidents.entity.guardian.socket

import com.google.gson.annotations.SerializedName

data class AudioPing(
    val amount: Int = 0,
    val number: Int = 0,
    val buffer: String = "",
    @SerializedName("read_size")
    val readSize: Int = 0
)
