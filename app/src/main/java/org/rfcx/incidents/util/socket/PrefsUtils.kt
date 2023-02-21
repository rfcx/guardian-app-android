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
}
