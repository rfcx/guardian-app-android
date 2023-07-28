package org.rfcx.incidents.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import jp.wasabeef.glide.transformations.BlurTransformation
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

object ImageUtils {

    const val FILE_CONTENT_PROVIDER = BuildConfig.APPLICATION_ID + ".provider"
    const val REQUEST_TAKE_PHOTO = 4001
    const val REQUEST_GALLERY = 5001

    // region Take a photo
    fun createImageFile(): File {
        val directoryName = "RFCx-Guardian-App"
        val imageFileName = "IMG_${Date().time}"
        val directory = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).absolutePath,
            directoryName
        )

        if (!directory.exists()) {
            directory.mkdir()
        }

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            directory /* directory */
        )
    }

    fun createImageFile(image: Uri, context: Context): String? {
        val contentResolver = context.contentResolver ?: return null
        val filePath = (
            context.applicationInfo.dataDir + File.separator +
                System.currentTimeMillis() + ".jpg"
            )

        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(image) ?: return null
            val outputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (ignore: IOException) {
            return null
        }
        return file.absolutePath
    }
}

fun ImageView.setDeploymentImage(url: String, blur: Boolean, fromServer: Boolean, token: String? = null, progressBar: ProgressBar? = null) {
    var placeholder = R.drawable.bg_placeholder_dark
    if (fromServer) {
        progressBar?.visibility = View.VISIBLE

        val glideUrl = GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        )

        Glide.with(this)
            .load(glideUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar?.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar?.visibility = View.GONE
                    return false
                }
            })
            .placeholder(placeholder)
            .error(placeholder)
            .into(this)
    } else {
        if (blur) {
            Glide.with(this)
                .load(url)
                .placeholder(placeholder)
                .error(placeholder)
                .transform(MultiTransformation(BlurTransformation(15, 1)))
                .into(this)
        } else {
            Glide.with(this)
                .load(url)
                .placeholder(placeholder)
                .error(placeholder)
                .into(this)
        }
    }
}
