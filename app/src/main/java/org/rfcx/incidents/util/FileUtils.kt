package org.rfcx.incidents.util

import android.webkit.MimeTypeMap
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object FileUtils {
    // default for json file
    fun File.getMimeType(fallback: String = "application/geo+json"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault())) }
            ?: fallback
    }
}
