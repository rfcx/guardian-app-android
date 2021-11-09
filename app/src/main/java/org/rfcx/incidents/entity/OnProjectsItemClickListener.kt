package org.rfcx.incidents.entity

import org.rfcx.incidents.view.login.ProjectsItem

interface OnProjectsItemClickListener {
	fun onItemClick(item: ProjectsItem, position: Int)
	fun onLockImageClicked()
}
