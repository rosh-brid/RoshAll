package lib.action

import android.view.View

class Click(private val v: View) {
    fun one(aksi: () -> Unit) {
        v.setOnClickListener {
            v.animate()
                .scaleX(0.50f)
                .scaleY(0.50f)
                .setDuration(90)
                .withEndAction {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(90)
                        .withEndAction {
                            aksi()
                        }
                        .start()
                }
                .start()
        }
    }

    fun long(aksi: () -> Unit) {
        v.setOnLongClickListener {
            aksi()
            true
        }
    }

    // 3. Klik Ganda (Double Click)
    fun double(aksi: () -> Unit) {
        var lastClickTime = 0L
        
        v.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            
            // Jika jarak klik kurang dari 300ms, anggap sebagai double click
            if (currentTime - lastClickTime < 300) {
                aksi()
                lastClickTime = 0L // Reset setelah double click terdeteksi
            } else {
                lastClickTime = currentTime
            }
        }
    }
}