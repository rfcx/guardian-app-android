package org.rfcx.incidents.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.rfcx.incidents.R

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

    @BindingAdapter("setProfileImage")
    @JvmStatic
    fun setProfile(imageView: ImageView, context: Context) {
        Glide.with(imageView.context).load(context.getUserProfile())
            .placeholder(R.drawable.bg_circle_grey)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }
}
