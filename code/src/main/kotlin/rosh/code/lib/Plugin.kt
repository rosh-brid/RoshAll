package rosh.code.lib

import android.app.Activity
import org.json.JSONObject
import java.io.File

class Plugin(private val kelas: Activity) {

    fun folderPlugin(): File {
        val f = File(kelas.getExternalFilesDir(null), "plugins")
        if (!f.exists()) f.mkdirs()
        return f
    }

    fun listPlugin(): MutableList<String> {
        val hasil = mutableListOf<String>()
        val folders = folderPlugin().listFiles() ?: return hasil

        for (folder in folders) {
            if (!folder.isDirectory) continue
            val manifest = File(folder, "plugin.json")
            if (!manifest.exists()) continue
            try {
                val json = JSONObject(manifest.readText())
                hasil.add(json.optString("name", folder.name))
            } catch (e: Exception) {
                hasil.add("${folder.name} (rusak)")
            }
        }
        return hasil
    }

    // Ambil grammar dari plugin aktif — dipanggil PengelolaBahasa
    fun grammarAktif(): MutableList<File> {
        val hasil = mutableListOf<File>()
        for (folder in folderPlugin().listFiles() ?: return hasil) {
            val manifest = File(folder, "plugin.json")
            if (!folder.isDirectory || !manifest.exists()) continue
            try {
                val json = JSONObject(manifest.readText())
                val main = json.optString("main", "")
                if (main.isNotEmpty()) {
                    val g = File(folder, main)
                    if (g.exists()) hasil.add(g)
                }
            } catch (e: Exception) {}
        }
        return hasil
    }
}
