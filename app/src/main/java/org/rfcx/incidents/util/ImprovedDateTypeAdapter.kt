package org.rfcx.incidents.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class ImprovedDateTypeAdapter : TypeAdapter<Date>() {
    
    override fun write(out: JsonWriter?, value: Date?) {
        if (value == null) {
            out?.nullValue()
            return
        }
    }
    
    override fun read(`in`: JsonReader?): Date {
        if (`in` != null) {
            return deserializeToDate(`in`.nextString())
        }
        return Date()
    }
    
    @Synchronized
    private fun deserializeToDate(json: String): Date {
        return try {
            Date(java.lang.Long.parseLong(json))
        } catch (e: Exception) {
            Date()
        }
    }
}
