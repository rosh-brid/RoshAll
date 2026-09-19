package rosh.terminal

import rosh.terminal.exec.ShellCustom
import rosh.terminal.exec.ShellSession
import rosh.lib.widget.TerminalView
import rosh.lib.aksi.Klik
import rosh.lib.aksi.Download
import rosh.lib.aksi.UnZip

import android.os.*
import android.widget.*
import android.view.*
import android.content.*
import android.net.Uri
import android.provider.Settings

import java.io.File

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.result.contract.ActivityResultContracts

class Terminal : AppCompatActivity(){

    private lateinit var pusat: DrawerLayout
    private lateinit var terminal: TerminalView
    private lateinit var mode: Switch
    private val pilihRoot =
    registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

        if (uri == null) return@registerForActivityResult

        try {
            salinRoot(uri)
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "Error take file",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terminal)

        PasangId()
        Awal()
        Tombol()
    }

    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
        terminal = findViewById(R.id.terminal)
        mode = findViewById(R.id.mode)
    }

    private fun Awal() {
       pusat.post{cekRoot()}
    }
    
    private fun cekRoot() {
        val bash = File(filesDir, "bin/bash")
        val gz = File(filesDir, "ubuntu.tar.gz")

        if (!gz.exists()) { DapatkanRoot() }
        else { if (!bash.exists()) { ExtrakRoot(gz) }
            else { AturMode() }
        }
    }
    
    private fun DapatkanRoot(){
        val item = LayoutInflater.from(this).inflate(R.layout.pop_dapatkan_root, null)
        val internal = item.findViewById<TextView>(R.id.internal)
        val download = item.findViewById<TextView>(R.id.download)
        val pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        val root = window.decorView.rootView
        
        Klik(internal).sekali{
            pilihRoot.launch( arrayOf( "application/gzip", "application/x-gzip", "application/octet-stream" ) )
        }
        
        Klik(download).sekali{
            pop.dismiss()
            val d = Download(this)
            val ub = "https://cdimage.ubuntu.com/ubuntu-base/releases/25.10/release/ubuntu-base-25.10-base-arm64.tar.gz"
            d.url(ub)
            d.setName("ubuntu.tar.gz")
            d.setDir(filesDir.absolutePath)
            d.setOnSelesai{_,_->
                ExtrakRoot(File(filesDir, "ubuntu.tar.gz"))
            }
            d.start()
        }
        
        pop.showAtLocation(root, Gravity.CENTER, 0, 0)
    }
    
    private fun ExtrakRoot(terima:File){
        val u = UnZip(this)
        u.setDir(filesDir.absolutePath)
        u.setTarget(terima.absolutePath)
        u.setOnSelesai { sukses ->
            if (sukses) {
                File(terima.absolutePath).delete()
                AturMode()
            }
        }
        u.startGz()
    }    
    
    private fun salinRoot(uri: Uri) {
        val target = File(filesDir, "ubuntu.tar.gz")

        contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        ExtrakRoot(target)
    }

    private fun Tombol() {
        mode.setOnCheckedChangeListener { _, _ -> AturMode() }
    }

    private fun AturMode() {
        val shell = rosh.terminal.exec.Shell(this,terminal)
        if(mode.isChecked){
            mode.text = "System"
            shell.statusShell(true)
        }else{
            mode.text = "Custom"
            shell.statusShell(false)
        }
    }
    
    
}