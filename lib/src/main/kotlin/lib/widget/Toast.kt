package lib.widget

import android.app.Activity

class Toast(private val kelas: Activity) {

    private var pesan: String = ""

    fun setMessage(terima: String): Toast {
        pesan = terima
        return this
    }

    fun show() {
        android.widget.Toast.makeText(
            kelas,
            pesan,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}