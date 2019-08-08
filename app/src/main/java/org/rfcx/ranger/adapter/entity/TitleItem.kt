package org.rfcx.ranger.adapter.entity

import org.rfcx.ranger.view.status.adapter.StatusAdapter

class TitleItem(var title: String) : BaseItem, StatusAdapter.StatusItemBase {
    override fun getViewType(): Int = StatusAdapter.StatusItemBase.ITEM_TITLE
}