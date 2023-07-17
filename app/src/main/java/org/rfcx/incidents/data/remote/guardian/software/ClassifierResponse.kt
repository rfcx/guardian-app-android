package org.rfcx.incidents.data.remote.guardian.software

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ClassifierResponse(
    @Expose val id: String,
    override val name: String,
    override val version: String,
    @SerializedName("path") override val url: String,
    @Expose @SerializedName("type") val classifierType: String,
    @Expose override val sha1: String,
    @Expose @SerializedName("sample_rate") val sampleRate: String,
    @Expose @SerializedName("input_gain") val inputGain: String,
    @Expose @SerializedName("window_size") val windowSize: String,
    @Expose @SerializedName("step_size") val stepSize: String,
    @Expose val classifications: String,
    @Expose @SerializedName("classifications_filter_threshold") val classificationsFilterThreshold: String
) : GuardianFileResponse
