package rosh.lib.aksi

import rosh.lib.R
import android.app.Activity
import android.widget.*
import android.view.*
import java.io.File

class UnZip(private val kelas: Activity) {
    private val item = LayoutInflater.from(kelas).inflate(R.layout.pop_unzip, null)
    private val pop = PopupWindow(item, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true)

    private var dir: String? = null
    private var target: String = ""

    private val namaZip = item.findViewById<TextView>(R.id.nama_zip)
    private val persen = item.findViewById<TextView>(R.id.persen)
    private val ukuran = item.findViewById<TextView>(R.id.ukuran)
    private val namaProses = item.findViewById<TextView>(R.id.nama_proses)
    private val anakProgress = item.findViewById<LinearLayout>(R.id.anak_view_persen)
    private val indukProgress = item.findViewById<LinearLayout>(R.id.view_persen)
    private val btnBatal = item.findViewById<TextView>(R.id.n)

    private var onSelesai: ((Boolean) -> Unit)? = null
    private var batal = false
    private var proses: Process? = null

    init {
        btnBatal.setOnClickListener {
            batal = true
            try { proses?.destroy() } catch (_: Exception) {}
            pop.dismiss()
        }
    }

    fun setDir(terima: String?) { dir = terima }
    fun setTarget(terima: String) {
        target = terima
        namaZip.text = File(terima).name
    }
    fun setOnSelesai(listener: (Boolean) -> Unit) { onSelesai = listener }

    fun startGz() = jalankan(true)  
    fun startZip() = jalankan(false) 

    private fun jalankan(isTar: Boolean) {
        mulaiUlang()
        val root = kelas.window.decorView.rootView
        pop.showAtLocation(root, Gravity.CENTER, 0, 0)

        Thread {
            var sukses = false
            try {
                val basis = folderTujuan()
                val src = kutip(target)
                val dst = kutip(basis.absolutePath)

                val cmdHitung = if (isTar)
                    "tar -tzf $src 2>/dev/null | wc -l"
                else
                    "unzip -Z1 $src 2>/dev/null | wc -l"

                val total = shell(cmdHitung).trim().toLongOrNull() ?: 0L

                val cmdEkstrak = if (isTar)
                    "tar -xzvf $src -C $dst 2>&1"
                else
                    "unzip -o $src -d $dst 2>&1"

                val p = ProcessBuilder("sh", "-c", cmdEkstrak)
                    .redirectErrorStream(true)
                    .start()
                proses = p

                var dihitung = 0L
                p.inputStream.bufferedReader().useLines { aliran ->
                    for (baris in aliran) {
                        if (batal) break
                        val nama = bersihkanBaris(baris, isTar)
                        if (nama.isEmpty()) continue
                        dihitung++
                        perbaruiUI(total, dihitung, nama)
                    }
                }

                val kode = p.waitFor()
                sukses = !batal && kode == 0
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                proses = null
            }
            selesai(sukses)
        }.start()
    }

    private fun shell(cmd: String): String {
        val p = ProcessBuilder("sh", "-c", cmd).start()
        val hasil = p.inputStream.bufferedReader().readText()
        p.waitFor()
        return hasil
    }

    private fun kutip(path: String): String =
        "'" + path.replace("'", "'\\''") + "'"

    private fun bersihkanBaris(baris: String, isTar: Boolean): String {
        var s = baris.trim()
        if (s.isEmpty()) return ""

        if (!isTar) {
            val i = s.indexOf(": ")
            if (i >= 0 && s.substringBefore(":").let { pref ->
                    pref == "inflating" || pref == "extracting" ||
                    pref == "creating" || pref == "linking"
                }) {
                s = s.substring(i + 2).trim()
            }
            if (s.startsWith("Archive:") || s.startsWith("replace ")) return ""
            return s
        }

        val regexGnu = Regex("^[dl\\-bcps][rwxstT\\-]{9}[.+]?\\s+\\S+/\\S+\\s+\\d+\\s+[\\d\\-]+\\s+[\\d:]+\\s+(.*)$")
        regexGnu.find(s)?.let { return it.groupValues[1] }
        return s
    }


    private fun mulaiUlang() {
        batal = false
        persen.text = "0%"
        ukuran.text = "0 entri"
        namaProses.text = ""
        kelas.runOnUiThread {
            val lp = anakProgress.layoutParams
            lp.width = 0
            anakProgress.layoutParams = lp
        }
    }

    private fun selesai(sukses: Boolean) {
    kelas.runOnUiThread {
        when {
            sukses -> {
                persen.text = "100%"
                namaProses.text = "Selesai"
                pop.dismiss()
            }

            batal -> {
                // batal
            }

            else -> {
                namaProses.text = "Gagal!"
                Toast.makeText(
                    kelas,
                    "Ekstraksi gagal",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        onSelesai?.invoke(sukses)
    }
}

    private fun folderTujuan(): File {
        val d = dir
        return if (!d.isNullOrEmpty()) File(d).also { it.mkdirs() }
        else File(target).parentFile!!
    }

    private fun perbaruiUI(total: Long, dihitung: Long, namaEntri: String) {
        val p = if (total > 0) ((dihitung * 100) / total).coerceIn(0, 100).toInt() else 0
        kelas.runOnUiThread {
            persen.text = "$p%"
            ukuran.text = "$dihitung entri"
            namaProses.text = namaEntri
            indukProgress.post {
                val lebar = indukProgress.width
                if (lebar > 0) {
                    val lp = anakProgress.layoutParams
                    lp.width = (lebar * p / 100f).toInt()
                    anakProgress.layoutParams = lp
                }
            }
        }
    }
}
