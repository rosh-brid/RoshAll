package rosh.lib.exec

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TutupService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == AKSI_TUTUP) {
            context.stopService(Intent(context, ShellCustom::class.java))
        }
    }

    companion object {
        const val AKSI_TUTUP = "rosh.lib.exec.TUTUP_TERMINAL"
    }
}
