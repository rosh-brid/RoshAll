package rosh.terminal.exec

import rosh.terminal.R
import rosh.lib.widget.TerminalView
import rosh.lib.aksi.Klik

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*

import androidx.core.app.NotificationCompat

class ShellCustom : Service() {

    private lateinit var windowManager: WindowManager

    private var balon: ImageView? = null
    private var posisi: WindowManager.LayoutParams? = null
    private var popupView: View? = null
    private var popupParams: WindowManager.LayoutParams? = null

    companion object {
        private const val CHANNEL_ID = "terminal"
        private const val NOTIF_ID = 1001
        const val AKSI_MATIKAN = "rosh.terminal.MATIKAN"
    }

    override fun onCreate() {
        super.onCreate()
        buatChannel()
        pushNotif()
        pushAlertWindow()

        // service hidup = pastikan shell hidup (misal start langsung dari popup "full"→activity)
        ShellSession.mulai(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) { AKSI_MATIKAN -> stopSelf() }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun pushNotif() {
        val matikan = Intent(this, ShellCustom::class.java).apply { action = AKSI_MATIKAN }
        val pendingMatikan = PendingIntent.getService(
            this, 0, matikan,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(rosh.lib.R.drawable.terminal)
            .setContentTitle("Terminal on")
            .setContentText("Terminal is Running")
            .setOngoing(true)
            .addAction(0, "OFF", pendingMatikan)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun buatChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Terminal", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Status terminal Rosh" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun pushAlertWindow() {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val view = ImageView(this).apply {
            setImageResource(rosh.lib.R.drawable.terminal)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val params = WindowManager.LayoutParams(
            40.dp(), 40.dp(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 15.dp()
            y = 200.dp()
        }

        posisi = params
        balon = view
        gerakBalon(view, params)
        windowManager.addView(view, params)
    }

    private fun gerakBalon(view: ImageView, params: WindowManager.LayoutParams) {
        var awalX = 0; var awalY = 0
        var sentuhX = 0f; var sentuhY = 0f
        var geser = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    awalX = params.x; awalY = params.y
                    sentuhX = event.rawX; sentuhY = event.rawY
                    geser = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - sentuhX
                    val dy = event.rawY - sentuhY
                    if (kotlin.math.abs(dx) > 10 || kotlin.math.abs(dy) > 10) geser = true
                    params.x = awalX - dx.toInt()
                    params.y = awalY + dy.toInt()
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> { if (!geser) bukaPopup(); true }
                else -> false
            }
        }
    }

    // ── POPUP TERMINAL MINI ─────────────────────────────

    private fun bukaPopup() {
    if (popupView != null) return   // sudah terbuka

    val view = LayoutInflater.from(this).inflate(R.layout.pop_alert_terminal, null)

    // JANGAN pakai FLAG_NOT_FOCUSABLE — TerminalView butuh fokus
    // supaya keyboard (IME) bisa muncul di dalam overlay.
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        // posisikan di tengah layar
        x = 20.dp()
        y = 300
    }

    val minimize = view.findViewById<View>(R.id.minimize)
    val full     = view.findViewById<View>(R.id.full)
    val close    = view.findViewById<View>(R.id.close)
    val geser    = view.findViewById<View>(R.id.tempat_geser)
    val mini     = view.findViewById<TerminalView>(R.id.terminal_pop)

    // ── sambungkan TerminalView mini ke sesi shell ──
    val penyambung = object : ShellSession.Pendengar {
        override fun onOutput(teks: String) { mini.append(teks) }
        override fun onDirektori(dir: String) { mini.setDir(dir) }
        override fun onClear() { mini.textClear() }
        override fun onMati() { tutupPopup() }
    }

    mini.setOnCommandListener { cmd -> ShellSession.kirim(cmd) }

    Klik(minimize).sekali { tutupPopup() }

    Klik(close).sekali {
        ShellSession.hentikan()
        stopSelf()
    }

    Klik(full).sekali {
        tutupPopup()
        val intent = Intent(this, rosh.terminal.Terminal::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    gerakPopup(geser, params)

    // simpan penyambung agar bisa dilepas saat popup ditutup
    view.setTag(penyambung)

    popupView = view
    popupParams = params

    ShellSession.pasang(penyambung)
    windowManager.addView(view, params)
}

private fun tutupPopup() {
    val v = popupView ?: return
    (v.tag as? ShellSession.Pendengar)?.let { ShellSession.lepas(it) }
    if (v.parent != null) windowManager.removeView(v)
    popupView = null
    popupParams = null
}


    private fun gerakPopup(view: View, params: WindowManager.LayoutParams) {
    var awalX = 0; var awalY = 0
    var sentuhX = 0f; var sentuhY = 0f

    view.setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                awalX = params.x; awalY = params.y
                sentuhX = event.rawX; sentuhY = event.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = awalX + (event.rawX - sentuhX).toInt()
                params.y = awalY + (event.rawY - sentuhY).toInt()
                windowManager.updateViewLayout(popupView, params)
                true
            }
            else -> false
        }
    }
}

    override fun onDestroy() {
    tutupPopup()
    balon?.let { if (it.parent != null) windowManager.removeView(it) }
    balon = null
    posisi = null
    stopForeground(STOP_FOREGROUND_REMOVE)
    super.onDestroy()
}


    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
