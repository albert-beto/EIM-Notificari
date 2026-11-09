package ro.pub.cs.systems.eim.notificari

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Tot ce tine de notificari, intr-un singur loc.
 *
 * E un `object` (singleton), deci il apelezi direct, fara sa-l creezi:
 *     NotificationHelper.createChannel(this)
 *     NotificationHelper.show(this, "Titlu", "Text")
 *
 * Primeste `Context`, deci merge din ORICE componenta:
 *   - din activitate: this
 *   - din serviciu:   this   (Service e un Context)
 *   - din receiver:   context (parametrul din onReceive)
 *
 * LA COLOCVIU: poti copia fisierul asta asa cum e si doar schimbi pachetul de sus
 * si clasa activitatii de la linia cu PendingIntent.
 */
object NotificationHelper {

    // id-ul canalului: orice string, dar trebuie SA FIE ACELASI la creare si la folosire
    const val CHANNEL_ID = "eim_channel"

    // cheia cu care trimitem mesajul in activitate cand se apasa pe notificare
    const val EXTRA_MESSAGE = "message"

    /**
     * PASUL 3: CANALUL
     * - de la Android 8 (API 26), orice notificare trebuie sa apartina unui canal
     * - il creezi O DATA (de ex. in onCreate al activitatii)
     * - daca il creezi de mai multe ori cu acelasi id, nu se intampla nimic rau
     */
    fun createChannel(context: Context) {
        // verificarea e necesara pentru ca minSdk e 24, iar NotificationChannel exista abia de la 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,                              // id-ul (cod)
                "EIM notifications",                     // numele vazut de utilizator in Setari
                NotificationManager.IMPORTANCE_DEFAULT   // DEFAULT = sunet, apare in bara
                // IMPORTANCE_HIGH = apare si ca banner sus pe ecran (heads-up)
            )
            channel.description = "Notificari din aplicatia de colocviu"

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Verifica daca avem voie sa afisam notificari.
     * - sub Android 13 permisiunea e data automat -> true
     * - de la Android 13 trebuie sa o fi acceptat utilizatorul
     */
    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * PASUL 4: CONSTRUIREA SI AFISAREA NOTIFICARII
     *
     * @param id  id-ul notificarii:
     *            - ACELASI id -> notificarea noua O INLOCUIESTE pe cea veche
     *            - id DIFERIT -> apare inca o notificare
     */
    fun show(context: Context, title: String, text: String, id: Int = 1) {
        // fara permisiune nu facem nimic (altfel Android Studio da eroare de lint la notify)
        if (!hasPermission(context)) return

        // ---- OPTIONAL: ce se intampla cand apesi pe notificare ----
        // un Intent EXPLICIT catre activitatea pe care vrem sa o deschidem
        val openIntent = Intent(context, NotificationMainActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, text)
            // NEW_TASK: necesar cand pornim din serviciu/receiver (nu sunt activitati)
            // SINGLE_TOP: daca activitatea e deja sus, nu o recreeaza -> se apeleaza onNewIntent()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        // PendingIntent = un Intent "pastrat pentru mai tarziu", pe care il va declansa sistemul
        // cand utilizatorul apasa pe notificare
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,                                    // requestCode, de regula 0
            openIntent,
            // FLAG_IMMUTABLE e OBLIGATORIU de la Android 12, altfel crapa
            // FLAG_UPDATE_CURRENT: daca exista deja un PendingIntent, ii actualizeaza extras-urile
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // OBLIGATORIU! fara el -> crash
            .setContentTitle(title)                            // titlul (bold)
            .setContentText(text)                              // textul de sub titlu
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // pentru Android < 8 (fara canale)
            .setContentIntent(pendingIntent)                   // ce face la apasare (optional)
            .setAutoCancel(true)                               // dispare din bara dupa ce o apesi
            .build()

        // afisarea propriu-zisa
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
