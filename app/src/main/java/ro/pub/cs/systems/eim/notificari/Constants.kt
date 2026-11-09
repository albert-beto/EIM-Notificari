package ro.pub.cs.systems.eim.notificari

object Constants {
    // actiunea broadcast-ului din exemplul 2 (orice string unic; de obicei pachet + nume)
    const val ACTION_NOTIFY = "ro.pub.cs.systems.eim.notificari.NOTIFY"

    // cheia pentru textul trimis prin intent (catre receiver si catre serviciu)
    const val EXTRA_TEXT = "text"

    // requestCode pentru cererea permisiunii: orice int >= 0
    const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
}
