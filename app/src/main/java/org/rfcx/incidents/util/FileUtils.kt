package org.rfcx.incidents.util

import android.webkit.MimeTypeMap
import java.io.File
import java.util.Locale

object FileUtils {
    // default for json file
    fun File.getMimeType(fallback: String = "image/jpeg"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault())) }
            ?: fallback
    }
}
