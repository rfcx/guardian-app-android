package android.rfcx.org.ranger.adapter.entity

import android.rfcx.org.ranger.entity.message.Message
import java.util.*

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

open class MessageItem(var message: Message, itemType: Int, date: Date) : BaseItem(itemType, date)