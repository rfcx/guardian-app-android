package org.rfcx.incidents.util

import android.graphics.drawable.Drawable
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
import org.rfcx.incidents.R

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
