package org.rfcx.incidents.util

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import org.rfcx.incidents.R


fun ImageView.setPath(path: String) {
	val bitmap = BitmapFactory.decodeFile(path)
	setImageBitmap(bitmap)
}

fun ImageView.setImageProfile(url: String?) {
	Glide.with(this.context).load(url)
			.placeholder(R.drawable.bg_circle_grey)
			.apply(RequestOptions.circleCropTransform())
			.into(this)
}

fun ImageView.setReportImage(url: String, fromServer: Boolean, token: String? = null, progressBar: ProgressBar) {
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
