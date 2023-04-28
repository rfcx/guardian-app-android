package org.rfcx.incidents.entity.guardian.socket

import com.google.gson.annotations.SerializedName

data class AudioCaptureStatus(
    @SerializedName("is_audio_capturing")
    val isCapturing: Boolean,
    @SerializedName("audio_capturing_message")
    val msg: String?
)
