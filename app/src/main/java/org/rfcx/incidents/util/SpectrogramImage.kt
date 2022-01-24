package org.rfcx.incidents.util

import org.rfcx.incidents.entity.event.Event

class SpectrogramImage {
	fun setImage(event: Event?) : String {
		return "https://assets.rfcx.org/audio/${event?.audioId}.png?width=420&height=460&inline=1"
	}
}
