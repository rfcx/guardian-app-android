package org.rfcx.incidents.util.socket

import android.util.Base64
import org.rfcx.incidents.entity.guardian.socket.GuardianPing
import org.rfcx.incidents.util.socket.PingUtils.getClassifiers
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