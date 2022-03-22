package org.rfcx.incidents.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.rfcx.incidents.R

fun ImageView.setReportImage(
    url: String,
    fromServer: Boolean,
    token: String? = null,
    progressBar: ProgressBar,
    failedIcon: ImageView? = null,
    noneFoundText: TextView? = null
) {
    val placeholder = R.drawable.bg_placeholder_image
    if (fromServer) {
        progressBar.visibility = View.VISIBLE

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
                    progressBar.visibility = View.GONE
                    if (failedIcon != null && noneFoundText != null) {
                        failedIcon.visibility = View.VISIBLE
                        noneFoundText.visibility = View.VISIBLE
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE
                    if (failedIcon != null && noneFoundText != null) {
                        failedIcon.visibility = View.GONE
                        noneFoundText.visibility = View.GONE
                    }
                    return false
                }
            })
            .placeholder(placeholder)
            .error(placeholder)
            .into(this)
    } else {
        Glide.with(this)
            .load(url)
            .placeholder(placeholder)
            .error(placeholder)
            .into(this)
    }
}

fun ImageView.setImage(url: String?) {
    GlideApp.with(this)
        .load(url)
        .centerCrop()
        .into(this)
}
