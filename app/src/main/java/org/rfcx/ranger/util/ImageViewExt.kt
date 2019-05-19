package org.rfcx.ranger.util

import android.graphics.BitmapFactory
import android.widget.ImageView


fun ImageView.setPath(path: String) {
    val bitmap = BitmapFactory.decodeFile(path)
    setImageBitmap(bitmap)
}