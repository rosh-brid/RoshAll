package lib.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import lib.R

class TerminalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var warna: Paint
    private var warnaJalur: Int
    private var warnaBG: Int
    private var warnaFile: Int
    private var warnaFolder: Int
    private var warnaGlobalTeks: Int

    private var jalur: String
    
    // Buffer untuk teks yang sedang diketik
    private val contentBuffer = StringBuilder()
    
    // Menyimpan riwayat eksekusi terminal (multi-line)
    private val riwayatTerminal = mutableListOf<String>()

    // Konstanta ukuran font & padding
    private val textSizePx = 36f
    private val paddingPx = 20f

    init {
        // Agar View ini bisa menerima klik dan fokus dari keyboard
        isFocusable = true
        isFocusableInTouchMode = true

        warna = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.MONOSPACE
            textSize = textSizePx
        }
        
        warnaBG = ContextCompat.getColor(context, R.color.bg)
        warnaFile = ContextCompat.getColor(context, R.color.text)
        warnaFolder = Color.parseColor("#00ff00") // Pastikan import android.graphics.Color
        warnaGlobalTeks = ContextCompat.getColor(context, R.color.text)
        // Set warnaJalur cukup satu kali (aku pakai R.color.main sesuai kode awalmu)
        warnaJalur = ContextCompat.getColor(context, R.color.frist)

        jalur = "/"
        
        setOnClickListener {
            BukaKeyboard()
        }
    }

    override fun onDraw(kertas: Canvas) {
        super.onDraw(kertas)

        // 1. Gambar Background
        warna.color = warnaBG
        kertas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), warna)

        // 2. Tentukan posisi Y awal (batas atas + ukuran font)
        var posisiY = textSizePx + paddingPx

        // 3. Gambar Riwayat Hasil Terminal, fungsi ini mengembalikan posisi Y terakhir
        posisiY = HasilTerminal(kertas, posisiY)

        // 4. Gambar Baris Prompt yang sedang aktif
        PapanKetik(kertas, posisiY)
    }

    private fun HasilTerminal(kertas: Canvas, startY: Float): Float {
        var currentY = startY
        warna.color = warnaGlobalTeks
        
        for (baris in riwayatTerminal) {
            kertas.drawText(baris, paddingPx, currentY, warna)
            // Tambahkan jarak Y untuk baris berikutnya (spasi antar baris)
            currentY += textSizePx + 10f 
        }
        
        // Kembalikan currentY agar PapanKetik digambar di bawah riwayat
        return currentY
    }

    private fun PapanKetik(kertas: Canvas, posisiY: Float) {
        // Gambar prompt jalur direktori dengan warnanya
        warna.color = warnaJalur
        val prompt = "$jalur$ "
        kertas.drawText(prompt, paddingPx, posisiY, warna)

        // Hitung lebar prompt agar teks input berada tepat di sebelahnya
        val promptWidth = warna.measureText(prompt)

        // Gambar teks yang sedang diketik user
        warna.color = warnaGlobalTeks
        kertas.drawText(contentBuffer.toString(), paddingPx + promptWidth, posisiY, warna)
    }

    // Fungsi untuk menambah baris riwayat ke layar atas (misal hasil eksekusi perintah)
    fun tambahRiwayat(baris: String) {
        riwayatTerminal.add(baris)
        invalidate()
    }

    fun setText(terima: String) {
        contentBuffer.clear()
        contentBuffer.append(terima)
        invalidate()
    }

    fun append(terima: String) {
        contentBuffer.append(terima)
        invalidate()
    }

    // `warna` pakai default param null agar tidak harus selalu diisi
    fun setDir(terimaJalur: String, warnaUbah: Int? = null) {
        jalur = terimaJalur
        if (warnaUbah != null) {
            warnaJalur = warnaUbah
        }
        invalidate()
    }

    // --- COLOR SETTERS ---
    fun setBackground(color: Int) {
        warnaBG = color
        invalidate()
    }

    fun setTextColor(color: Int) {
        warnaGlobalTeks = color
        invalidate()
    }

    fun setDirColor(color: Int) {
        warnaJalur = color
        invalidate()
    }

    fun setFileExecColor(color: Int) {
        warnaFile = color
        invalidate()
    }

    // --- KEYBOARD ---
    private fun BukaKeyboard() {
        requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}
