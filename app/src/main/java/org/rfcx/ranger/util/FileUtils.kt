package org.rfcx.ranger.util

import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

object FileUtils {
	// default for json file
	fun File.getMimeType(fallback: String = "application/geo+json"): String {
		return MimeTypeMap.getFileExtensionFromUrl(toString())
				?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase(Locale.getDefault())) }
				?: fallback
	}
}
