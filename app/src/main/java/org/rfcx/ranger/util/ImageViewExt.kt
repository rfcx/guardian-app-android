package org.rfcx.ranger.util

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.rfcx.ranger.R


fun ImageView.setPath(path: String) {
    val bitmap = BitmapFactory.decodeFile(path)
    setImageBitmap(bitmap)
}

fun ImageView.setImageProfile(url: String) {
    Glide.with(this.context).load(url)
            .placeholder(R.drawable.bg_circle_grey)
            .apply(RequestOptions.circleCropTransform())
            .into(this)
}