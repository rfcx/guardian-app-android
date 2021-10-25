package org.rfcx.ranger.entity

import org.rfcx.ranger.view.login.ProjectsItem

interface OnProjectsItemClickListener {
	fun onItemClick(item: ProjectsItem, position: Int)
	fun onLockImageClicked()
}
