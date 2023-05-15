package org.rfcx.incidents.service.guardianfile

import android.content.Context
import okhttp3.ResponseBody
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class GuardianFileHelper(private val context: Context) {

    companion object {
        private const val DIR = "guardian-file"
    }

    suspend fun saveToDisk(response: ResponseBody?, targetFile: GuardianFile): String {
        if (response == null) throw Throwable("response cannot be null")
        return try {
            val dir = File(context.filesDir, "$DIR/${targetFile.type}")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val ext = if (targetFile.type == GuardianFileType.SOFTWARE.value) "apk.gz" else "tflite.gz"
            val name = if (targetFile.type == GuardianFileType.SOFTWARE.value) "${targetFile.name}-${targetFile.version}" else targetFile.id
            val file = File(dir, "$name.$ext")
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                inputStream = response.byteStream()
                outputStream = FileOutputStream(file)
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
                file.absolutePath
            } catch (e: IOException) {
                throw e
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            throw e
        }
    }

    suspend fun removeFromDisk(targetFile: GuardianFile): Result<Boolean> {
        val file = File(targetFile.path)
        if (file.exists()) {
            file.delete()
            return Result.Success(true)
        } else {
            throw Throwable("File not found")
        }
    }
}
