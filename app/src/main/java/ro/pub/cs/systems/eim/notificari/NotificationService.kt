package ro.pub.cs.systems.eim.notificari

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * EXEMPLUL 3: notificare dintr-un serviciu (started service).
 *
 * Serviciul porneste un thread care trimite 3 notificari, la 3 secunde distanta,
 * apoi se opreste singur cu stopSelf().
 *
 * `this` din serviciu E un Context, deci il dai direct lui NotificationHelper.
 * NotificationHelper se poate apela si din thread (nu e cod de interfata grafica).
 */
class NotificationService : Service() {

    private var thread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra(Constants.EXTRA_TEXT) ?: "fara text"

        // nu pornim un al doilea thread daca primul inca ruleaza
        if (thread == null) {
            thread = Thread {
                try {
                    for (i in 1..3) {
                        NotificationHelper.show(this, "Din serviciu ($i/3)", text, 200 + i)
                        Log.d("eimnotificari", "Notificare din serviciu $i/3")
                        Thread.sleep(3000)
                    }
                } catch (e: InterruptedException) {
                    // oprit din onDestroy (stopService) -> iesim
                }
                stopSelf()          // serviciul se opreste singur la final
            }
            thread?.start()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        thread?.interrupt()         // trezeste thread-ul din sleep, ca sa se termine
        thread = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null
}
