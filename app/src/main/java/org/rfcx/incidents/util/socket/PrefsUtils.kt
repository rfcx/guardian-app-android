package org.rfcx.incidents.util.socket

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.TimeZone

object PrefsUtils {
    fun canGuardianClassify(str: String): Boolean {
        val expect = listOf("sms", "sat", "")
        val json = JsonParser.parseString(str).asJsonObject
        val order = json.get("api_protocol_escalation_order").asString
        var canClassify = false
        expect.forEach {
            if (order.contains(it, false)) {
                canClassify = true
            }
        }
        return canClassify
    }
    fun getGuardianPlanFromPrefs(str: String?): GuardianPlan? {
        if (str == null) return null
        val json = JsonParser.parseString(str).asJsonObject
        return when (json.get("api_protocol_escalation_order").asString) {
            "mqtt,rest" -> GuardianPlan.CELL_ONLY
            "mqtt,rest,sms" -> GuardianPlan.CELL_SMS
            "sat" -> GuardianPlan.SAT_ONLY
            "" -> GuardianPlan.OFFLINE_MODE
            else -> null
        }
    }

    fun getSatTimeOffFromPrefs(str: String?): String? {
        if (str == null) return null
        val json = JsonParser.parseString(str).asJsonObject
        return json.get("api_satellite_off_hours").asString
    }

    fun getCellOnlyPrefs(): JsonObject {
        val prefs = JsonObject()
        prefs.addProperty("api_satellite_protocol", "off")
        prefs.addProperty("enable_audio_classify", "false")
        prefs.addProperty("enable_checkin_publish", "true")
        prefs.addProperty(
            "api_ping_cycle_fields",
            "checkins,instructions,prefs,sms,meta,detections,purged"
        )
        prefs.addProperty("enable_audio_cast", "true")
        prefs.addProperty("enable_file_socket", "true")
        prefs.addProperty("api_protocol_escalation_order", "mqtt,rest")
        prefs.addProperty("api_satellite_off_hours", "23:55-23:56,23:57-23:59")
        prefs.addProperty("admin_system_timezone", TimeZone.getDefault().id)
        prefs.addProperty("enable_reboot_forced_daily", "true")
        prefs.addProperty("api_ping_cycle_duration", "30")
        prefs.addProperty("api_ping_schedule_off_hours", "23:55-23:56,23:57-23:59")
        return prefs
    }

    fun getCellSMSPrefs(): JsonObject {
        val prefs = JsonObject()
        prefs.addProperty("api_satellite_protocol", "off")
        prefs.addProperty("enable_audio_classify", "true")
        prefs.addProperty("enable_checkin_publish", "true")
        prefs.addProperty(
            "api_ping_cycle_fields",
            "sms,battery,sentinel_power,software,detections,storage,memory,cpu"
        )
        prefs.addProperty("enable_audio_cast", "true")
        prefs.addProperty("enable_file_socket", "true")
        prefs.addProperty("api_protocol_escalation_order", "mqtt,rest,sms")
        prefs.addProperty("api_satellite_off_hours", "23:55-23:56,23:57-23:59")
        prefs.addProperty("admin_system_timezone", TimeZone.getDefault().id)
        prefs.addProperty("enable_reboot_forced_daily", "true")
        prefs.addProperty("api_ping_cycle_duration", "30")
        prefs.addProperty("api_ping_schedule_off_hours", "23:55-23:56,23:57-23:59")
        return prefs
    }

    fun getSatOnlyPrefs(timeOff: String): JsonObject {
        val prefs = JsonObject()
        prefs.addProperty("api_satellite_protocol", "swm")
        prefs.addProperty("enable_audio_classify", "true")
        prefs.addProperty("enable_checkin_publish", "false")
        prefs.addProperty(
            "api_ping_cycle_fields",
            "battery,sentinel_power,software,swm,detections,storage,memory,cpu"
        )
        prefs.addProperty("enable_audio_cast", "true")
        prefs.addProperty("enable_file_socket", "true")
        prefs.addProperty("api_protocol_escalation_order", "sat")
        prefs.addProperty("api_satellite_off_hours", timeOff)
        prefs.addProperty("admin_system_timezone", TimeZone.getDefault().id)
        prefs.addProperty("enable_reboot_forced_daily", "true")
        prefs.addProperty("api_ping_cycle_duration", "180")
        prefs.addProperty("api_ping_schedule_off_hours", "23:55-23:56,23:57-23:59")
        return prefs
    }

    fun getSatOnlyPrefs(): JsonObject {
        val prefs = JsonObject()
        prefs.addProperty("api_satellite_protocol", "swm")
        prefs.addProperty("enable_audio_classify", "true")
        prefs.addProperty("enable_checkin_publish", "false")
        prefs.addProperty(
            "api_ping_cycle_fields",
            "battery,sentinel_power,software,swm,detections,storage,memory,cpu"
        )
        prefs.addProperty("enable_audio_cast", "true")
        prefs.addProperty("enable_file_socket", "true")
        prefs.addProperty("api_protocol_escalation_order", "sat")
        prefs.addProperty("admin_system_timezone", TimeZone.getDefault().id)
        prefs.addProperty("enable_reboot_forced_daily", "true")
        prefs.addProperty("api_ping_cycle_duration", "180")
        prefs.addProperty("api_ping_schedule_off_hours", "23:55-23:56,23:57-23:59")
        return prefs
    }

    fun getOfflineModePrefs(): JsonObject {
        val prefs = JsonObject()
        prefs.addProperty("api_satellite_protocol", "off")
        prefs.addProperty("enable_audio_classify", "false")
        prefs.addProperty("enable_checkin_publish", "false")
        prefs.addProperty(
            "api_ping_cycle_fields",
            "checkins,instructions,prefs,sms,meta,detections,purged"
        )
        prefs.addProperty("enable_audio_cast", "true")
        prefs.addProperty("enable_file_socket", "true")
        prefs.addProperty("api_protocol_escalation_order", "")
        prefs.addProperty("api_satellite_off_hours", "23:55-23:56,23:57-23:59")
        prefs.addProperty("admin_system_timezone", TimeZone.getDefault().id)
        prefs.addProperty("enable_reboot_forced_daily", "true")
        prefs.addProperty("api_ping_cycle_duration", "30")
        prefs.addProperty("api_ping_schedule_off_hours", "00:00-23:59")
        return prefs
    }
}

enum class GuardianPlan {
    CELL_ONLY, CELL_SMS, SAT_ONLY, OFFLINE_MODE
}
