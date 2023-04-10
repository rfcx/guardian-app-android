package org.rfcx.incidents.entity.guardian.registration

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterResponse

@RealmClass
open class GuardianRegistration(
    @PrimaryKey var guid: String = "",
    var token: String = "",
    var keystorePassphrase: String = "L2Cevkmc9W5fFCKn",
    var pinCode: String = "",
    var apiMqttHost: String = "staging-api-mqtt.rfcx.org",
    var apiSmsAddress: String = "+14154803657",
    var env: String = "staging"
) : RealmModel {

    companion object {
        const val TABLE_NAME = "GuardianRegistration"
        const val FIELD_GUID = "guid"
        const val FIELD_TOKEN = "token"
        const val FIELD_KEYSTORE_PASSPHRASE = "keystorePassphrase"
        const val FIELD_PIN_CODE = "pinCode"
        const val FIELD_API_MQTT_HOST = "apiMqttHost"
        const val FIELD_API_SMS_ADDRESS = "apiSmsAddress"
        const val FIELD_ENV = "env"
    }
}

fun GuardianRegistration.toSocketFormat(): GuardianRegisterResponse {
    return GuardianRegisterResponse(guid, token, keystorePassphrase, pinCode, apiMqttHost, apiSmsAddress)
}

fun GuardianRegistration.toRequest(): GuardianRegisterRequest {
    return GuardianRegisterRequest(guid, token, pinCode)
}
