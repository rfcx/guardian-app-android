package android.rfcx.org.ranger.entity

import android.rfcx.org.ranger.entity.event.Event
import io.realm.RealmList
import io.realm.RealmObject

/**
 * Created by Anuphap Suwannamas on 10/22/2017 AD.
 * Email: Anupharpae@gmail.com
 */

open class EventResponse : RealmObject() {
    var events: RealmList<Event>? = null
}