package org.rfcx.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.rfcx.ranger.entity.RangerLocation
import org.rfcx.ranger.repo.api.SendLocationApi
import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import android.os.Build



/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
class SendLocationReceiver : BroadcastReceiver() {

    private val tag = SendLocationReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(Intent(context, SaveLocationService::class.java))
        } else {
            context?.startService(Intent(context, SaveLocationService::class.java))
        }

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        //  delete sent data
        val sentRangerLocations: RealmResults<RangerLocation> = realm.where(RangerLocation::class.java)
                .equalTo(RangerLocation.keyIsSent, true).findAll()
        sentRangerLocations.deleteAllFromRealm()

        val rangerLocations: RealmResults<RangerLocation> = realm.where(RangerLocation::class.java)
                .equalTo(RangerLocation.keyIsSent, false).findAll()
        val rangerLocationsUse = realm.copyFromRealm(rangerLocations)

        realm.commitTransaction()
        realm.close()
        Log.d(tag, "rangerLocationsUse need to send ${rangerLocationsUse.size}")
        for (rangerLocation in rangerLocationsUse) {
            sendLocation(context, rangerLocation)
        }
    }

    private fun sendLocation(context: Context?, rangerLocation: RangerLocation) {
        if (context == null) return

        SendLocationApi().checkIn(context,
                rangerLocation.latitude, rangerLocation.longitude
                , rangerLocation.time, object : SendLocationApi.SendLocationCallBack {
            override fun onSuccess() {

                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                rangerLocation.isSent = true
                realm.copyToRealmOrUpdate(rangerLocation)
                realm.commitTransaction()
				realm.close()
                Log.d(tag, "onSuccess ${rangerLocation.latitude} ${rangerLocation.longitude} ${rangerLocation.time}")
            }

            override fun onFailed(t: Throwable?, message: String?) {
                message?.let {
                    Log.e(tag, it)
                }
            }
        })
    }

}