package org.rfcx.incidents.entity.user

/**
 * Returned from an Auth0 login or token refresh request
 */

data class UserAuthResponse(
    val guid: String,
    val email: String?,
    val nickname: String?,
    val idToken: String,
    val accessToken: String?,
    val refreshToken: String?,
    val roles: Set<String> = setOf(),
    val accessibleSites: Set<String> = setOf(),
    val defaultSite: String? = null,
    val picture: String?,
    val expiredAt: Long?
) {

    val isRanger: Boolean get() = roles.contains("rfcxUser") && defaultSite != null
}
