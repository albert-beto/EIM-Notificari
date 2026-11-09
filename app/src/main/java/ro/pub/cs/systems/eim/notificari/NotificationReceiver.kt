package ro.pub.cs.systems.eim.notificari

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * EXEMPLUL 2: notificare dintr-un BroadcastReceiver.
 *
 * Diferenta fata de activitate: nu ai `this` ca activitate, ci primesti `context`
 * ca parametru in onReceive(). Il dai mai departe lui NotificationHelper.
 *
 * Tipic la colocviu: "cand vine un mesaj cu difuzare de la serviciu, afisati o notificare".
 * Atunci codul din onReceive() de aici se pune in receiverul tau.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra(Constants.EXTRA_TEXT) ?: "fara text"
        Log.d("eimnotificari", "Broadcast primit: $text")

        NotificationHelper.show(context, "Din receiver", text, 100)
        // id fix (100): fiecare broadcast nou INLOCUIESTE notificarea anterioara a receiverului
    }
}
