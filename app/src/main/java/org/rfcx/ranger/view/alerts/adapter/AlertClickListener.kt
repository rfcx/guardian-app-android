package org.rfcx.ranger.view.alerts.adapter

import org.rfcx.ranger.entity.event.Event

interface AlertClickListener {
    fun onClickedAlert(event: Event)
}