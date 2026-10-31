package rosh.all

import lib.app.RoshActivity
import lib.action.Click
import lib.app.Dialog

import android.os.*
import android.widget.*
import android.content.*
import android.content.res.Configuration

import java.io.File

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate

class Main : RoshActivity() {

    private lateinit var pusat:DrawerLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setView(R.layout.activity_main)
        
        PasangId()
        Tombol()
        Awal()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
    }
    
    private fun Awal(){
        AturTema()
    }
    
    private fun GantiTema(){
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        
        AturTema()
    }
    
    private fun AturTema(){
        val gt = findViewById<ImageView>(R.id.gambar_tema)
        val nt = findViewById<TextView>(R.id.nama_tema)
        
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if(dark){
            gt.setImageResource(lib.R.drawable.moon)
            nt.text = "Dark"
        }else{
            gt.setImageResource(lib.R.drawable.sun)
            nt.text = "Light"
        }
    }
    
    private fun Keluar(){
        Dialog(this)
            .setTitle("Exit")
            .setMessage("you will exit")
            .setPositiveButton("sure"){finish()}
            .setNegativeButton("cencel", null)
            .show()
    }
    
    private fun Tombol(){
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(pusat.isDrawerOpen(GravityCompat.START)){
                        pusat.closeDrawer(GravityCompat.START)
                    }else{Keluar()}
                }
            }
        )
        
        Click(findViewById<ImageView>(R.id.nav)).one{
            pusat.openDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.tutup)).one{
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.keluar)).one{
            Keluar()
        }
        
        Click(findViewById<LinearLayout>(R.id.tema)).one{
            GantiTema()
        }
        
        Click(findViewById<LinearLayout>(R.id.font)).one{
            lib.view.ListFont(this).show()
        }
        
        Click(findViewById<ImageView>(R.id.github)).one{
            BukaGit()
        }
        
        Click(findViewById<LinearLayout>(R.id.berkas)).one{
            startActivity(Intent(this, rosh.berkas.Berkas::class.java))
        }
        
        Click(findViewById<LinearLayout>(R.id.code)).one{
            startActivity(Intent(this, rosh.code.Code::class.java))
        }
        
        Click(findViewById<LinearLayout>(R.id.terminal)).one{
            startActivity(Intent(this, rosh.terminal.Terminal::class.java))
        }
        
        Click(findViewById<LinearLayout>(R.id.jaringan)).one{
            val intent = Intent("android.intent.action.MAIN").apply {
                setClassName(
                    "com.android.phone",
                    "com.android.phone.settings.RadioInfo"
                )
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        
        Click(findViewById<LinearLayout>(R.id.gambar)).one{
            startActivity(Intent(this, rosh.gambar.Gambar::class.java))
        }
        
        Click(findViewById<LinearLayout>(R.id.video)).one{
            startActivity(Intent(this, rosh.video.Video::class.java))
        }
        
        Click(findViewById<LinearLayout>(R.id.browser)).one{
            startActivity(Intent(this, rosh.browser.Browser::class.java))
        }
    }
    
    private fun BukaGit() {
        val repo = "https://github.com/rosh-brid/RoshAll"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(repo)
        }

        startActivity(intent)
    }

}
