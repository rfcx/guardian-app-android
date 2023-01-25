package org.rfcx.incidents.view.guardian

interface GuardianDeploymentEventListener {
    fun setupToolbar()
    fun showToolbar()
    fun hideToolbar()
    fun setToolbarTitle(title: String)

    fun changeScreen(screen: GuardianScreen)
}
