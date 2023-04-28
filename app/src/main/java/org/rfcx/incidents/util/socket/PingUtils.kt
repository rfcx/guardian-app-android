package org.rfcx.incidents.util.socket

import android.util.Base64
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.rfcx.incidents.entity.guardian.socket.AdminPing
import org.rfcx.incidents.entity.guardian.socket.AudioCaptureStatus
import org.rfcx.incidents.entity.guardian.socket.GuardianPing
import org.rfcx.incidents.entity.guardian.socket.I2CAccessibility
import org.rfcx.incidents.entity.guardian.socket.SentinelBattery
import org.rfcx.incidents.entity.guardian.socket.SentinelInput
import org.rfcx.incidents.entity.guardian.socket.SentinelPower
import org.rfcx.incidents.entity.guardian.socket.SentinelSystem
import org.rfcx.incidents.entity.guardian.socket.SpeedTest
import org.rfcx.incidents.util.socket.PingUtils.getAudioParameter
import org.rfcx.incidents.util.socket.PingUtils.getGuardianLocalTime
import org.rfcx.incidents.util.socket.PingUtils.getPrefsSha1
import org.rfcx.incidents.util.socket.PingUtils.isRegistered
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

object PingUtils {

    fun GuardianPing.getSoftware(): Map<String, String>? {
        val software = this.software ?: return null
        val softwareList = software.split("|")
        val mapSoftwareVersion = mutableMapOf<String, String>()
        softwareList.forEach {
            val role = it.split("*")[0]
            val version = it.split("*")[1]
            mapSoftwareVersion[role] = version
        }
        return mapSoftwareVersion
    }

    fun GuardianPing.getClassifiers(): Map<String, String> {
        val library = this.library ?: return mapOf()
        library.let { lib ->
            if (lib.has("classifiers")) {
                val classifiers = lib.get("classifiers").asJsonArray
                if (classifiers.size() > 0) {
                    val map = mutableMapOf<String, String>()

                    classifiers.forEach { clsf ->
                        map[clsf.asJsonObject.get("guid").asString.split("-v")[0]] = clsf.asJsonObject.get("guid").asString.split("-v")[1]
                    }
                    return map
                }
                return mapOf()
            }
            return mapOf()
        }
    }

    fun GuardianPing.getActiveClassifiers(): Map<String, String> {
        val library = this.activeClassifier ?: return mapOf()
        library.let { lib ->
            val activeClassifiers = lib.asJsonArray
            if (activeClassifiers.size() > 0) {
                val map = mutableMapOf<String, String>()
                activeClassifiers.forEach { clsf ->
                    map[clsf.asJsonObject.get("guid").asString.split("-v")[0]] = clsf.asJsonObject.get("guid").asString.split("-v")[1]
                }
                return map
            }
            return mapOf()
        }
    }

    fun GuardianPing.canGuardianClassify(): Boolean {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return false
            return PrefsUtils.canGuardianClassify(Gson().toJson(prefs))
        }
        return false
    }

    fun GuardianPing.getInternalBattery(): Int? {
        val battery = this.battery ?: return null
        return battery.split("*")[1].toInt()
    }

    fun AdminPing.getI2cAccessibility(): I2CAccessibility? {
        val i2c = this.companion?.get("i2c")?.asJsonObject ?: return null
        return Gson().fromJson(i2c, I2CAccessibility::class.java)
    }

    fun AdminPing.getSentinelPower(): SentinelPower? {
        val sentinelPower = this.sentinelPower ?: return null
        val splitSentinelPower = sentinelPower.split("|")
        var system = SentinelSystem()
        var input = SentinelInput()
        var batt = SentinelBattery()
        try {
            splitSentinelPower.forEach {
                val splittedItem = it.split("*")
                when (splittedItem[0]) {
                    "system" -> system = SentinelSystem(
                        splittedItem[2].toInt(),
                        splittedItem[3].toInt(),
                        splittedItem[4].toInt(),
                        splittedItem[5].toInt()
                    )
                    "input" -> input = SentinelInput(
                        splittedItem[2].toInt(),
                        splittedItem[3].toInt(),
                        splittedItem[4].toInt(),
                        splittedItem[5].toInt()
                    )
                    "battery" -> batt = SentinelBattery(
                        splittedItem[2].toInt(),
                        splittedItem[3].toInt(),
                        splittedItem[4].toDouble(),
                        splittedItem[5].toInt()
                    )
                }
            }
        } catch (e: NumberFormatException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return SentinelPower(input, system, batt)
    }

    fun AdminPing.getSimDetected(): Boolean? {
        return this.companion?.get("sim_info")?.asJsonObject?.get("has_sim")?.asBoolean
            ?: return null
    }

    fun AdminPing.getGPSDetection(): Boolean? {
        return this.companion?.get("sat_info")?.asJsonObject?.get("is_gps_connected")?.asBoolean
            ?: return null
    }

    fun AdminPing.getSwarmId(): String? {
        return this.companion?.get("sat_info")?.asJsonObject?.get("sat_id")?.asString
            ?: return null
    }

    fun AdminPing.getSimNetwork(): Int? {
        val network = this.network ?: return null
        return network.split("*")[1].toInt()
    }

    fun GuardianPing.getSwarmNetwork(): Int? {
        val network = this.swm ?: return null
        val splitNetworks = network.split("|").map { it.split("*") }
        return splitNetworks.last()[1].toIntOrNull()
    }

    fun AdminPing.getSpeedTest(): SpeedTest? {
        val speedTest = this.companion?.get("speed_test")?.asJsonObject ?: return null
        val downloadSpeed = speedTest.get("download_speed").asDouble
        val uploadSpeed = speedTest.get("upload_speed").asDouble
        val isFailed = speedTest.get("is_failed").asBoolean
        val isTesting =
            if (speedTest.has("is_testing")) speedTest.get("is_testing").asBoolean else false
        val hasConnection = speedTest.get("connection_available").asBoolean
        return SpeedTest(downloadSpeed, uploadSpeed, isFailed, isTesting, hasConnection)
    }

    fun AdminPing.getPhoneNumber(): String? {
        return this.companion?.get("sim_info")?.asJsonObject?.get("phone_number")?.asString
            ?: return null
    }

    fun GuardianPing.getGuardianLocalTime(): Long? {
        val localtime = this.companion?.get("system_time_utc") ?: return null
        return localtime.asLong
    }

    fun GuardianPing.getGuardianTimezone(): String? {
        val timezone = this.companion?.get("system_timezone") ?: return null
        return timezone.asString
    }

    fun GuardianPing.getGuardianPlan(): GuardianPlan? {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return null
            return PrefsUtils.getGuardianPlanFromPrefs(Gson().toJson(prefs))
        }
        return null
    }

    fun GuardianPing.getSatTimeOff(): String? {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return null
            return PrefsUtils.getSatTimeOffFromPrefs(Gson().toJson(prefs))
        }
        return null
    }

    fun GuardianPing.getPrefsSha1(): String? {
        if (this.prefs is JsonObject) {
            val sha1 = this.prefs.get("sha1") ?: return null
            return sha1.asString
        }
        return null
    }

    fun GuardianPing.isRegistered(): Boolean? {
        val isRegistered = this.companion?.get("is_registered") ?: return null
        return isRegistered.asBoolean
    }

    fun GuardianPing.getGuid(): String? {
        val guid = this.companion?.get("guardian")?.asJsonObject?.get("guid") ?: return null
        return guid.asString
    }

    fun GuardianPing.getAudioParameter(): JsonObject? {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return null
            return PrefsUtils.stringToAudioPrefs(Gson().toJson(prefs))
        }
        return null
    }

    fun GuardianPing.getSampleRate(): Int? {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return null
            return PrefsUtils.getSampleRateFromPrefs(Gson().toJson(prefs))
        }
        return null
    }

    fun GuardianPing.getAudioCaptureStatus(): AudioCaptureStatus? {
        val isCapturing = this.companion?.get("is_audio_capturing") ?: return null
        val captureMsg = this.companion.get("audio_capturing_message") ?: null
        return AudioCaptureStatus(isCapturing.asBoolean, captureMsg?.asString)
    }

    fun unGzipString(content: String?): String? {
        return gZipByteArrayToUnGZipString(content)
    }

    private fun base64StringToByteArray(base64String: String?): ByteArray? {
        return Base64.decode(base64String, Base64.NO_WRAP)
    }

    private fun gZipByteArrayToUnGZipString(content: String?): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            val gZIPInputStream = GZIPInputStream(ByteArrayInputStream(base64StringToByteArray(content)))
            var res = 0
            val buf = ByteArray(1024)
            while (res >= 0) {
                res = gZIPInputStream.read(buf, 0, buf.size)
                if (res > 0) {
                    byteArrayOutputStream.write(buf, 0, res)
                }
            }
        } catch (e: Exception) {
            return content
        }
        return byteArrayOutputStream.toString()
    }
}
