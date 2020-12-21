package org.rfcx.ranger.util

import org.rfcx.ranger.entity.event.Event

class SpectrogramImage {
	fun setImage(event: Event?) : String {
		return "https://assets.rfcx.org/audio/${event?.audioId}.png?width=450&height=350&inline=1"
	}
}
