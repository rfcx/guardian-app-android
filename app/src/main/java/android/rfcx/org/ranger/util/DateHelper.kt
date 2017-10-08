package android.rfcx.org.ranger.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class DateHelper {

    companion object {
        private val messageInputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private val messageOutputFormat = "yyyy-MM-dd-HH:mm"
        fun getMessageDateTime(input: String): String {
            val sdf = SimpleDateFormat(messageInputFormat, Locale.getDefault())
            return try {
                val d: Date = sdf.parse(input)
                val sdf2 = SimpleDateFormat(messageOutputFormat, Locale.getDefault())
                sdf2.format(d)
            } catch (e: Exception) {
                ""
            }
        }

        fun getIsoTime(): String {
            // pattern 2008-09-15T15:53:00+05:00
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            return try {
                val d = Date(System.currentTimeMillis())
                return sdf.format(d)
            } catch (e: Exception) {
                ""
            }
        }
    }

}