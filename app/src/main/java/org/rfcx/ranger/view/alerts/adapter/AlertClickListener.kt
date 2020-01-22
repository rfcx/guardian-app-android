package org.rfcx.ranger.view.alerts.adapter

import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.EventItem

interface AlertClickListener {
    fun onClickedAlert(event: Event, state: EventItem.State)
}