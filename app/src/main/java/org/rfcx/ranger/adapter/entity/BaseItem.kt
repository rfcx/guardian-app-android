package org.rfcx.ranger.adapter.entity

import java.util.*

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

open class BaseItem(var itemType: Int, var date: Date) {
    companion object {
        val ITEM_EVENT_TYPE: Int = 1
        val ITEM_MESSAGE_TYPE: Int = 2
    }
}