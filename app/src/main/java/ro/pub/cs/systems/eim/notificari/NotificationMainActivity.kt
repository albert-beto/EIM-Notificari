package ro.pub.cs.systems.eim.notificari

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Activitatea principala: arata TOATE cele 3 moduri de a trimite o notificare
 *   1. direct din activitate (butonul "Notify")
 *   2. dintr-un BroadcastReceiver (butonul "Broadcast")
 *   3. dintr-un serviciu (butonul "Service")
 * si ce se intampla cand utilizatorul apasa pe notificare (onCreate / onNewIntent).
 */
class NotificationMainActivity : AppCompatActivity() {

    private lateinit var editTextMessage: EditText

    // receiverul din exemplul 2 (vezi NotificationReceiver.kt)
    private val notificationReceiver = NotificationReceiver()
    private val intentFilter = IntentFilter(Constants.ACTION_NOTIFY)

    private var notificationId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_main)

        editTextMessage = findViewById(R.id.EditTextMessage)

        // PASUL 2: cererea permisiunii la rulare (doar o activitate poate face asta)
        requestNotificationPermission()

        // PASUL 3: canalul (o singura data)
        NotificationHelper.createChannel(this)

        // ---------- EXEMPLUL 1: notificare direct din activitate ----------
        findViewById<Button>(R.id.ButtonNotify).setOnClickListener {
            if (!NotificationHelper.hasPermission(this)) {
                Toast.makeText(this, "Nu ai permis notificarile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // id diferit la fiecare apasare -> se aduna mai multe notificari in bara
            // (cu acelasi id, de ex. 1, s-ar inlocui mereu aceeasi notificare)
            NotificationHelper.show(this, "Din activitate", getMessage(), notificationId++)
        }

        // ---------- EXEMPLUL 2: notificare dintr-un BroadcastReceiver ----------
        findViewById<Button>(R.id.ButtonBroadcast).setOnClickListener {
            val intent = Intent(Constants.ACTION_NOTIFY)
            intent.putExtra(Constants.EXTRA_TEXT, getMessage())
            intent.setPackage(packageName)          // broadcast doar catre aplicatia mea
            sendBroadcast(intent)                   // -> NotificationReceiver.onReceive()
        }

        // ---------- EXEMPLUL 3: notificare dintr-un serviciu ----------
        findViewById<Button>(R.id.ButtonService).setOnClickListener {
            val intent = Intent(this, NotificationService::class.java)  // intent EXPLICIT
            intent.putExtra(Constants.EXTRA_TEXT, getMessage())
            startService(intent)                    // -> NotificationService.onStartCommand()
        }

        // daca aplicatia a fost DESCHISA DE LA ZERO prin apasarea pe notificare,
        // mesajul vine in intent-ul cu care a fost pornita activitatea
        showMessageFromNotification(intent)
    }

    /**
     * Apelata cand apesi pe notificare si activitatea EXISTA DEJA (e in fundal / pe stiva).
     * Merge datorita lui launchMode="singleTop" (manifest) sau FLAG_ACTIVITY_SINGLE_TOP.
     * Fara ea, mesajul din notificare s-ar pierde.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)                           // ca getIntent() sa intoarca intent-ul nou
        showMessageFromNotification(intent)
    }

    // receiverul asculta doar cat activitatea e vizibila
    override fun onResume() {
        super.onResume()
        // RECEIVER_NOT_EXPORTED: primeste doar broadcast-uri din aplicatia mea
        // (fara flag, pe Android 14+ aplicatia crapa cu SecurityException)
        ContextCompat.registerReceiver(
            this, notificationReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        unregisterReceiver(notificationReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        stopService(Intent(this, NotificationService::class.java))
        super.onDestroy()
    }

    /**
     * PASUL 2: PERMISIUNEA LA RULARE
     * - doar pe Android 13+ (TIRAMISU = API 33)
     * - la prima pornire apare dialogul "Allow notifications?"
     * - daca utilizatorul apasa "Don't allow", notificarile NU apar (dar nu crapa nimic)
     * - ca sa o resetezi in emulator: Settings > Apps > aplicatia > Notifications
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasPermission(this)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                Constants.NOTIFICATION_PERMISSION_REQUEST_CODE   // orice int >= 0, de ex. 1
            )
        }
    }

    /** Raspunsul utilizatorului la dialogul de permisiune (optional, doar ca feedback). */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            Toast.makeText(
                this,
                if (granted) "Notificari permise" else "Notificari refuzate",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getMessage(): String {
        val text = editTextMessage.text.toString()
        return if (text.isEmpty()) "Salut de la EIM!" else text
    }

    /** Afiseaza mesajul primit prin notificare (daca exista). */
    private fun showMessageFromNotification(intent: Intent?) {
        val message = intent?.getStringExtra(NotificationHelper.EXTRA_MESSAGE)
        if (message != null) {
            Toast.makeText(this, "Din notificare: $message", Toast.LENGTH_LONG).show()
        }
    }
}
