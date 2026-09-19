package rosh.terminal.exec

import android.content.Context
import android.os.Handler
import android.os.Looper

import java.io.*

import kotlin.concurrent.thread

object ShellSession {

    private const val DIR_MARK   = "@@DIR@@"      
    private const val NAMA_PROOT = "libproot.so"   
    private const val MAX_RIWAYAT = 200_000       

    var modeProot: Boolean = false
        private set

    interface Pendengar {
        fun onOutput(teks: String)    
        fun onDirektori(dir: String)   
        fun onClear()                  
        fun onMati()                   
    }

    private val ui = Handler(Looper.getMainLooper())
    private val pendengar = LinkedHashSet<Pendengar>()
    private val riwayat = StringBuilder()

    private var appContext: Context? = null
    private var proses: Process? = null
    private var out: BufferedReader? = null
    private var input: BufferedWriter? = null

    val hidup: Boolean get() = proses?.isAlive == true

    fun mulai(context: Context) {
        appContext = context.applicationContext
        if (hidup) return

        thread(name = "shell-main") {

            var builder = try {
                if (modeProot) buatProot() else buatSistem()
            } catch (e: Exception) {
                lapor("proot gagal: ${e.message}\n— fallback ke sh system —\n")
                modeProot = false
                try { buatSistem() } catch (e2: Exception) {
                    siarkan("shell error: ${e2.message}\n") { it.onMati() }
                    return@thread
                }
            }

            val p = try { builder.start() } catch (e: Exception) {
                siarkan("shell start error: ${e.message}\n") { it.onMati() }
                return@thread
            }
            proses = p
            out   = BufferedReader(InputStreamReader(p.inputStream))
            input = BufferedWriter(OutputStreamWriter(p.outputStream))

            Tulis("echo \"$DIR_MARK\$PWD\"")

            while (true) {
                val baris = try { out?.readLine() } catch (e: Exception) { null } ?: break

                if (baris.startsWith(DIR_MARK)) {
                    val dir = baris.removePrefix(DIR_MARK)
                    ui.post { pendengar.toList().forEach { it.onDirektori(dir) } }
                } else {
                    simpan(baris + "\n")
                    ui.post { pendengar.toList().forEach { it.onOutput(baris + "\n") } }
                }
            }

            if (proses === p) {
                proses = null
                ui.post { pendengar.toList().forEach { it.onMati() } }
            }
        }
    }

    fun gantiMode(proot: Boolean) {
        if (modeProot == proot && hidup) return
        modeProot = proot

        val ctx = appContext ?: return

        matikanSenyap()  

        val label = if (proot) "[proot]" else "[system]"
        simpan("\n$label shell restarted\n")
        ui.post { pendengar.toList().forEach { it.onOutput("\n$label shell restarted\n") } }

        mulai(ctx)
    }

    fun hentikan() {
        matikanSenyap()
        ui.post { pendengar.toList().forEach { it.onMati() } }
    }

    private fun matikanSenyap() {
        val p = proses
        proses = null
        try { input?.close(); out?.close(); p?.destroy() } catch (_: Exception) {}
    }

    private fun buatSistem(): ProcessBuilder {
        val files = appContext!!.filesDir
        val home  = File(files, "home").apply { mkdirs() }
        val tmp   = File(files, "tmp").apply  { mkdirs() }

        return ProcessBuilder("sh").apply {
            redirectErrorStream(true)
            directory(files)
            environment().apply {
                put("HOME",  home.absolutePath)
                put("TMPDIR", tmp.absolutePath)
                put("TERM",  "xterm-256color")
                put("PS1",   "")                          
                put("PATH",  "/system/bin:/system/xbin:/vendor/bin")
                put("LANG",  "C.UTF-8")
            }
        }
    }

    private fun buatProot(): ProcessBuilder {
    val ctx = appContext!!
    val files = ctx.filesDir
    val tmpDir = File(files, "tmp").apply { mkdirs() }

    val native = ctx.applicationInfo.nativeLibraryDir
    val proot = "$native/$NAMA_PROOT"
    val loader = "$native/libloader.so"
    val loader32 = "$native/libloader32.so"

    if (!File(proot).exists()) {
        throw IOException("proot not found: $proot")
    }

    val adaBash = File(files, "bin/bash").exists()
    val adaSh = File(files, "bin/sh").exists()

    if (!adaBash && !adaSh) {
        throw IOException(
            "rootfs kosong: ${files.absolutePath} (tidak ada /bin/sh)"
        )
    }

    val shellDalam = if (adaBash) "/bin/bash" else "/bin/sh"

    val linker =
        if (File("/system/bin/linker64").exists())
            "/system/bin/linker64"
        else
            "/system/bin/linker"

    val args = listOf(
        linker,
        proot,

        "--kill-on-exit",
        "--link2symlink",
        "--sysvipc",

        "-0",
        "-L",
        "-k", "5.3.0",

        "-r", files.absolutePath,
        "-w", "/",

        "-b", "/dev:/dev",
        "-b", "/proc",
        "-b", "/sys",
        "-b", "${tmpDir.absolutePath}:/tmp",
        "-b", "/storage",
        "-b", "/sdcard:/sdcard",

        "-m", "/dev/urandom:/dev/random",

        shellDalam
    )

    return ProcessBuilder(args).apply {
        redirectErrorStream(true)
        directory(files)

        environment().apply {
            put("PROOT_LOADER", loader)

            if (File(loader32).exists()) {
                put("PROOT_LOADER_32", loader32)
            }

            put("HOME", "/root")
            put("USER", "root")
            put("LOGNAME", "root")
            put("HOSTNAME", "localhost")

            put(
                "PATH",
                "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
            )

            put("TERM", "xterm-256color")
            put("TMPDIR", "/tmp")
            put("LANG", "C.UTF-8")
            put("LC_ALL", "C.UTF-8")
            put("PS1", "")

            put("PROOT_TMP_DIR", tmpDir.absolutePath)
            put("PROOT_LOADER", loader)

if (File(loader32).exists()) {
    put("PROOT_LOADER_32", loader32)
}

put("LD_LIBRARY_PATH", native)
            put("LD_LIBRARY_PATH", native)
        }
    }
}

    fun pasang(l: Pendengar) {
        ui.post {
            if (pendengar.add(l) && riwayat.isNotEmpty()) {
                l.onOutput(riwayat.toString())     // UI baru langsung dapat riwayat penuh
            }
        }
    }

    fun lepas(l: Pendengar) {
        ui.post { pendengar.remove(l) }
    }

    fun kirim(cmd: String) {
        val perintah = cmd.trim()

        when (perintah) {
            "clear", "cls" -> {
                ui.post {
                    riwayat.setLength(0)
                    pendengar.toList().forEach { it.onClear() }
                }
                return
            }

            "exit", "quit" -> {
                hentikan()
                return
            }

            "get-proot-dir" -> {
                val ctx = appContext ?: return
                val jalur = ctx.applicationInfo.nativeLibraryDir + "/" + NAMA_PROOT
                val teks = jalur + if (File(jalur).exists()) "\n" else "  (not found!)\n"
                simpan(teks)
                ui.post { pendengar.toList().forEach { it.onOutput(teks) } }
                return
            }
        }

        val ctx = appContext ?: return
        if (!hidup) mulai(ctx)

        thread(name = "shell-write") {
            try {
                Tulis(perintah)
                Tulis("echo \"$DIR_MARK\$PWD\"")   // refresh prompt setelah perintah
            } catch (e: Exception) {
                ui.post { pendengar.toList().forEach { it.onOutput("write error: ${e.message}\n") } }
            }
        }
    }

    private fun Tulis(perintah: String) {
        input?.apply {
            write(perintah)
            newLine()
            flush()
        }
    }

    private fun simpan(teks: String) {
        riwayat.append(teks)
        if (riwayat.length > MAX_RIWAYAT) {
            riwayat.delete(0, riwayat.length - MAX_RIWAYAT)
        }
    }

    private fun lapor(pesan: String) {
        simpan(pesan)
        ui.post { pendengar.toList().forEach { it.onOutput(pesan) } }
    }

    private fun siarkan(pesan: String, extra: (Pendengar) -> Unit = {}) {
        simpan(pesan)
        ui.post {
            pendengar.toList().forEach {
                it.onOutput(pesan)
                extra(it)
            }
        }
    }
}
