package org.rfcx.ranger.util

object PrefKey {
	private const val PREFIX = "org.rfcx.ranger:"
	const val ID_TOKEN = "${PREFIX}ID_TOKEN"
	const val ACCESS_TOKEN = "${PREFIX}ACCESS_TOKEN"
	const val GU_ID = "${PREFIX}GU_ID"
	const val DEFAULT_SITE = "${PREFIX}SITE"
	const val EMAIL = "${PREFIX}EMAIL"
	const val HAS_SUBSCRIBED_TO_DEFAULT_SITE = "${PREFIX}HAS_SUBSCRIBED_TO_DEFAULT_SITE"
	
	const val ENABLE_LOCATION_TRACKING = "${PREFIX}ENABLE_LOCATION_TRACKING"
}