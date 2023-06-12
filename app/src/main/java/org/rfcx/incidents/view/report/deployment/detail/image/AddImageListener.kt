package org.rfcx.incidents.view.report.deployment.detail.image

import org.rfcx.incidents.view.guardian.checklist.photos.Image

interface AddImageListener {
    fun saveImages(images: List<Image>)
    fun getImages(): List<Image>
    fun openDetailScreen()
}
