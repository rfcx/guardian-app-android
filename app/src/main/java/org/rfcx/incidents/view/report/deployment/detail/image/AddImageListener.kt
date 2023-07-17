package org.rfcx.incidents.view.report.deployment.detail.image

import kotlinx.coroutines.flow.StateFlow
import org.rfcx.incidents.view.guardian.checklist.photos.Image

interface AddImageListener {
    fun saveImages(images: List<Image>)
    fun getImages(): StateFlow<List<Image>>
    fun openDetailScreen()
}
