# EIM – Notificări în Android (exemplu comentat)

Exemplu de referință pentru Colocviul 1 EIM: cum trimiți o notificare în bara de notificări, din **activitate**, din **BroadcastReceiver** și din **serviciu**, plus ce se întâmplă când utilizatorul apasă pe ea.

## Unde e fiecare lucru

| Fișier | Ce conține |
|---|---|
| [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) | permisiunea `POST_NOTIFICATIONS`, `launchMode="singleTop"`, declararea serviciului |
| [`NotificationHelper.kt`](app/src/main/java/ro/pub/cs/systems/eim/notificari/NotificationHelper.kt) | **tot ce ține de notificare**: canal, verificarea permisiunii, construirea și afișarea, `PendingIntent` |
| [`NotificationMainActivity.kt`](app/src/main/java/ro/pub/cs/systems/eim/notificari/NotificationMainActivity.kt) | cererea permisiunii la rulare, cele 3 exemple, `onNewIntent` |
| [`NotificationReceiver.kt`](app/src/main/java/ro/pub/cs/systems/eim/notificari/NotificationReceiver.kt) | notificare din `onReceive` |
| [`NotificationService.kt`](app/src/main/java/ro/pub/cs/systems/eim/notificari/NotificationService.kt) | notificări dintr-un thread al unui serviciu |
| [`Constants.kt`](app/src/main/java/ro/pub/cs/systems/eim/notificari/Constants.kt) | chei și acțiuni |

## Cei 4 pași, pe scurt

**1. Manifest** (în afara lui `<application>`):
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**2. Permisiunea la rulare** (Android 13+, doar dintr-o activitate, în `onCreate`):
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
}
```

**3. Canalul** (Android 8+, o singură dată, în `onCreate`):
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel("eim_channel", "EIM", NotificationManager.IMPORTANCE_DEFAULT)
    getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}
```

**4. Notificarea:**
```kotlin
val notification = NotificationCompat.Builder(context, "eim_channel")
    .setSmallIcon(android.R.drawable.ic_dialog_info)   // OBLIGATORIU
    .setContentTitle("Titlu")
    .setContentText("Text")
    .setAutoCancel(true)
    .build()
NotificationManagerCompat.from(context).notify(1, notification)
```

`context` = `this` în activitate și în serviciu, `context` (parametrul) în `onReceive`.

## Cum folosești la colocviu

Copiezi `NotificationHelper.kt` în proiect, schimbi `package`-ul de sus și clasa activității din `PendingIntent`, apoi:
```kotlin
// în onCreate al activității
NotificationHelper.createChannel(this)
// + cererea permisiunii (pasul 2)

// oriunde ai un Context
NotificationHelper.show(context, "Titlu", "Text")
```

## Erori frecvente

| Simptom | Cauză |
|---|---|
| Notificarea nu apare, fără nicio eroare | permisiunea nu e acceptată (verifică Settings › Apps › aplicația › Notifications) sau lipsește din manifest |
| Crash `IllegalArgumentException: Invalid notification (no valid small icon)` | lipsește `setSmallIcon` |
| Crash la `PendingIntent.getActivity` | lipsește `FLAG_IMMUTABLE` (obligatoriu de la Android 12) |
| Notificarea nu apare pe Android 8+ | canalul nu a fost creat sau `CHANNEL_ID` diferă între creare și builder |
| Apare o singură notificare, care se tot înlocuiește | folosești același `id` la `notify` (e corect dacă asta vrei) |
| La apăsare, mesajul nu ajunge în activitatea deja deschisă | lipsește `onNewIntent` (+ `singleTop` / `FLAG_ACTIVITY_SINGLE_TOP`) |
| Lint error „Call requires permission” la `notify` | lipsește verificarea `checkSelfPermission` înainte de `notify` |

## Dependențe

Toate clasele sunt din `androidx.core` (inclus implicit în proiectele noi din Android Studio). Nu trebuie adăugat nimic în Gradle.
