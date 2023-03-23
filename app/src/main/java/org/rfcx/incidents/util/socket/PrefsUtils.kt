package org.rfcx.incidents.util.socket

import com.google.gson.JsonParser

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
}

enum class GuardianPlan {
    CELL_ONLY, CELL_SMS, SAT_ONLY, OFFLINE_MODE
}
