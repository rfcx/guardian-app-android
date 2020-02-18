package org.rfcx.ranger.util

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


fun ImageView.setPath(path: String) {
    val bitmap = BitmapFactory.decodeFile(path)
    setImageBitmap(bitmap)
}

fun ImageView.setImageProfile(url: String) {
    Glide.with(this.context).load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}