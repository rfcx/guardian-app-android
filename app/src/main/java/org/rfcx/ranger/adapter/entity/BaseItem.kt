package org.rfcx.ranger.adapter.entity

import java.util.*

open class BaseItem(var itemType: Int, var date: Date) {
    companion object {
        val ITEM_EVENT_TYPE: Int = 1
        val ITEM_MESSAGE_TYPE: Int = 2
    }
}