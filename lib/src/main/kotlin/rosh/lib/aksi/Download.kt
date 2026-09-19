package rosh.lib.aksi

import rosh.lib.R
import android.app.Activity
import android.view.*
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class Download(private val kelas: Activity) {

    private lateinit var item: View

    private lateinit var namaUrl: TextView
    private lateinit var persen: TextView
    private lateinit var ukuran: TextView
    private lateinit var proses: ProgressBar
    private lateinit var viewPersen: View
    private lateinit var anakViewPersen: View
    private lateinit var pop: PopupWindow

    private var link = ""
    private var jalan = true
    private var setDirPath: String? = null
    private var namaFileDownload: String? = null

    private var onSelesai: ((sukses: Boolean, file: File?) -> Unit)? = null

    init {
        item = LayoutInflater.from(kelas)
            .inflate(R.layout.pop_unduh_langsung, null)

        namaUrl = item.findViewById(R.id.nama_url)
        persen = item.findViewById(R.id.persen)
        ukuran = item.findViewById(R.id.ukuran)
        proses = item.findViewById(R.id.proses)
        viewPersen = item.findViewById(R.id.view_persen)
        anakViewPersen = item.findViewById(R.id.anak_view_persen)
        pop = PopupWindow(item, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)

        item.findViewById<TextView>(R.id.n).setOnClickListener {
            jalan = false
            pop.dismiss()
        }
    }

    fun url(link: String) {
        this.link = link
    }

    fun setDir(line: String?) {
        setDirPath = line
    }

    fun setName(terima: String) {
        namaFileDownload = terima
    }

    fun setOnSelesai(listener: (sukses: Boolean, file: File?) -> Unit) {
        onSelesai = listener
    }

    fun start() {
        if (link.isEmpty()) return

        jalan = true

        namaUrl.text = link
        persen.text = "0%"
        ukuran.text = "0mb"
        anakViewPersen.layoutParams = anakViewPersen.layoutParams.also { it.width = 0 }

        val root = kelas.window.decorView.rootView
        pop.showAtLocation(root, Gravity.CENTER, 0, 0)

        Thread {
            unduh()
        }.start()
    }

    private fun unduh() {
        var koneksi: HttpURLConnection? = null
        var file: File? = null

        try {
            koneksi = URL(link).openConnection() as HttpURLConnection
            koneksi.connectTimeout = 15000
            koneksi.readTimeout = 15000
            koneksi.connect()

            val kode = koneksi.responseCode
            if (kode !in 200..299) throw java.io.IOException("HTTP $kode")

            val total = koneksi.contentLengthLong
            var sudah = 0L

            val input = koneksi.inputStream

            val fileName = if (namaFileDownload != null) {
                namaFileDownload
            } else {
                link.substringAfterLast("/").let {
                    if (it.contains("?")) it.substringBefore("?") else it
                }.ifEmpty { "tmp.file" }
            }

            file = if (setDirPath != null) {
                val dir = File(setDirPath!!)
                if (!dir.exists()) dir.mkdirs()
                File(dir, fileName)
            } else {
                File(kelas.cacheDir, fileName)
            }

            val output = FileOutputStream(file)
            val buffer = ByteArray(8192)

            while (jalan) {
                val baca = input.read(buffer)
                if (baca == -1) break

                output.write(buffer, 0, baca)
                sudah += baca

                val p = if (total > 0) {
                    ((sudah * 100) / total).toInt()
                } else {
                    0
                }

                kelas.runOnUiThread {
                    persen.text = "$p%"
                    ukuran.text = "${ukuran(sudah)} / ${ukuran(total)}"
                    proses.progress = p

                    val induk = viewPersen.width
                    if (induk > 0) {
                        val params = anakViewPersen.layoutParams
                        params.width = (induk * p) / 100
                        anakViewPersen.layoutParams = params
                    }
                }
            }

            output.close()
            input.close()

            if (jalan) {
                kelas.runOnUiThread {
                    persen.text = "100%"
                    ukuran.text = ukuran(sudah)
                    pop.dismiss()
                    onSelesai?.invoke(true, file)   
                }
            } else {
                file.delete()
                kelas.runOnUiThread {
                    onSelesai?.invoke(false, null)  
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            file?.delete() 
            kelas.runOnUiThread {
                if (jalan) {
                    Toast.makeText(
                        kelas,
                        "Gagal mengunduh: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    pop.dismiss()
                }
                onSelesai?.invoke(false, null)     
            }
        } finally {
            koneksi?.disconnect()
        }
    }

    private fun ukuran(byte: Long): String {
        if (byte <= 0) return "0mb"

        return when {
            byte >= 1024 * 1024 ->
                "%.2fmb".format(byte / 1024.0 / 1024.0)

            byte >= 1024 ->
                "%.2fkb".format(byte / 1024.0)

            else ->
                "${byte}b"
        }
    }
}
