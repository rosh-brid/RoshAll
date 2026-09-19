package rosh.terminal.exec

import rosh.lib.widget.TerminalView

import android.app.Activity

import java.io.*
import java.lang.Process

class Shell(
    private val kelas: Activity,
    private val terminal: TerminalView
) {

    private var proses: Process? = null
    private var out: BufferedReader? = null
    private var run: BufferedWriter? = null

    private var dir: String = "~/"
    private var status: Boolean = false


    fun statusShell(terima: Boolean?) {

        status = terima ?: false

        if (status) {
            Sistem()
        } else {
            Custom()
        }
    }


    private fun Sistem() {

        proses = ProcessBuilder("sh")
            .redirectErrorStream(true)
            .start()

        val p = proses ?: return

        run = p.outputStream.bufferedWriter()
        out = p.inputStream.bufferedReader()

        val input = run ?: return
        val output = out ?: return

        val dirAwal = kelas.filesDir.absolutePath


        /*
         * Membaca output dari shell.
         */
        Thread {

            try {

                while (true) {

                    val baris = output.readLine() ?: break

                    /*
                     * Marker direktori.
                     */
                    if (baris.startsWith("@@DIR@@")) {

                        dir = baris
                            .removePrefix("@@DIR@@")
                            .trim()

                        terminal.post {
                            terminal.setDir(dir)
                        }

                        continue
                    }


                    /*
                     * Output command biasa.
                     */
                    terminal.post {
                        terminal.append(baris + "\n")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }.start()


        /*
         * Direktori awal shell.
         */
        input.write("cd ${kutip(dirAwal)}")
        input.newLine()

        kirimDir()

        input.flush()


        /*
         * Command dari TerminalView.
         */
        terminal.setOnCommandListener { cmd ->

            when (cmd.lowercase()) {

                "exit" -> {
                    kelas.finish()
                }


                "clear" -> {
                    terminal.setText("")
                }


                "get-proot-dir" -> {

                    val i = File(
                        kelas.applicationInfo.nativeLibraryDir,
                        "libproot.so"
                    )

                    terminal.append(
                        i.absolutePath + "\n"
                    )
                }


                else -> {

                    try {

                        input.write(cmd)
                        input.newLine()

                        /*
                         * Setelah command,
                         * ambil direktori terbaru.
                         */
                        kirimDir()

                        input.flush()

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    /*
     * Mengirim lokasi PWD ke Shell
     * menggunakan marker internal.
     */
    private fun kirimDir() {

        val input = run ?: return

        input.write(
            "printf '@@DIR@@%s\\n' \"\$PWD\""
        )

        input.newLine()
    }


    /*
     * Mengamankan path untuk shell.
     */
    private fun kutip(path: String): String {

        return "'" +
            path.replace("'", "'\\''") +
            "'"
    }


    private fun Custom() {

        /*
         * Environment Custom.
         */
        val env = HashMap<String, String>()

        env["HOME"] =
            File(
                kelas.filesDir,
                "home"
            ).absolutePath


        /*
         * Proses Custom akan dibuat
         * di sini nanti.
         *
         * Contoh nantinya:
         *
         * proot
         *   ↓
         * Ubuntu
         *   ↓
         * bash
         */

        // TODO: ProcessBuilder Custom
    }
}