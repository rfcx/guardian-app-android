package org.rfcx.ranger.util

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter


object ImageViewDatabinding {
    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageDrawable(view: ImageView, drawable: Drawable) {
        view.setImageDrawable(drawable)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageResource(imageView: ImageView, @DrawableRes resource: Int) {
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, resource))
    }
}