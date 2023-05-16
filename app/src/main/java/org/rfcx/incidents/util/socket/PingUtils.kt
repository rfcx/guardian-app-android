package org.rfcx.incidents.util.socket

import android.content.Context
import android.util.Base64
import androidx.preference.Preference
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.rfcx.incidents.entity.guardian.socket.AdminPing
import org.rfcx.incidents.entity.guardian.socket.AudioCaptureStatus
import org.rfcx.incidents.entity.guardian.socket.GuardianArchived
import org.rfcx.incidents.entity.guardian.socket.GuardianPing
import org.rfcx.incidents.entity.guardian.socket.GuardianStorage
import org.rfcx.incidents.entity.guardian.socket.I2CAccessibility
import org.rfcx.incidents.entity.guardian.socket.SentinelBattery
import org.rfcx.incidents.entity.guardian.socket.SentinelInput
import org.rfcx.incidents.entity.guardian.socket.SentinelPower
import org.rfcx.incidents.entity.guardian.socket.SentinelSystem
import org.rfcx.incidents.entity.guardian.socket.SpeedTest
import org.rfcx.incidents.entity.guardian.socket.Storage
import org.rfcx.incidents.util.socket.PingUtils.getAudioParameter
import org.rfcx.incidents.util.socket.PingUtils.getGuardianLocalTime
import org.rfcx.incidents.util.socket.PingUtils.getPrefsSha1
import org.rfcx.incidents.util.socket.PingUtils.isRegistered
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

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

    fun AdminPing.getStorage(): GuardianStorage? {
        val storage = this.storage?.split("|") ?: return null
        return GuardianStorage(
            storage.getOrNull(0)?.let {
                val values = it.split("*")
                Storage(values[2].toLong(), values[2].toLong() + values[3].toLong())
            },
            storage.getOrNull(1)?.let {
                val values = it.split("*")
                Storage(values[2].toLong(), values[2].toLong() + values[3].toLong())
            }
        )
    }

    fun GuardianPing.getGuardianArchivedAudios(): List<GuardianArchived>? {
        val archived = this.companion?.get("archived-audio")?.asString ?: return null
        val listOfArchived = archived.split("|")
        return listOfArchived.map {
            val data = it.split("*")
            var missing: List<String>? = null
            if (data.size > 5) {
                missing = data.subList(5, data.size)
                if (missing.size == 1 && missing[0] == "") {
                    missing = null
                }
            }
            GuardianArchived(
                data[0].toLong(),
                data[1].toLong(),
                data[2].toInt(),
                data[3].toInt(),
                data[4].toInt(),
                if (missing.isNullOrEmpty()) null else missing
            )
        }
    }

    fun GuardianPing.getLatestCheckIn(): JsonObject? {
        if (this.prefs is JsonObject) {
            val checkIn = this.companion?.get("checkin") ?: return null
            return checkIn.asJsonObject
        }
        return null
    }

    fun GuardianPing.getSwarmUnsetMessages(): Int? {
        val swm = this.swm ?: return null
        val splitSwm = swm.split("|").map { it.split("*") }
        val lastSwmObj = splitSwm.last()
        return lastSwmObj[lastSwmObj.size - 1].toIntOrNull()
    }

    fun GuardianPing.getPrefs(context: Context): List<Preference> {
        if (this.prefs is JsonObject) {
            val prefs = this.prefs.get("vals") ?: return listOf()
            return PrefsUtils.stringToPrefs(context, Gson().toJson(prefs))
        }
        return listOf()
    }

    fun GuardianPing.getGuardianToken(): String? {
        val token = this.companion?.get("guardian")?.asJsonObject?.get("token") ?: return null
        return token.asString
    }

    fun getGuardianVital(adminPing: AdminPing?, guardianPing: GuardianPing?): String? {
        val admin = adminPing?.toJson()?.apply {
            remove("companion")
            remove("speed_test")
            remove("i2c")
            remove("sim_info")
        } ?: return null
        val guardian = guardianPing?.toJson()?.apply {
            remove("companion")
        } ?: return null
        val combinedPing = JsonObject()
        admin.keySet().forEach {
            combinedPing.add(it, admin.get(it))
        }
        guardian.keySet().forEach {
            combinedPing.add(it, guardian.get(it))
        }

        return gzip(Gson().toJson(combinedPing))
    }

    private fun gzip(content: String): String {

        val byteArrayOutputStream = ByteArrayOutputStream()

        val gZIPOutputStream: GZIPOutputStream?
        gZIPOutputStream = object : GZIPOutputStream(byteArrayOutputStream) {
            init {
                def.setLevel(Deflater.BEST_COMPRESSION)
            }
        }
        gZIPOutputStream.write(content.toByteArray(Charsets.UTF_8))

        gZIPOutputStream.close()

        return URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP), "UTF-8")
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
