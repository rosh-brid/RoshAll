package rosh.rosh

import rosh.lib.aksi.Klik
import rosh.rosh.pop.KeluarApp

import android.os.*
import android.widget.*
import android.content.*
import android.view.*
import android.content.res.Configuration
import android.graphics.*
import android.net.*

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback

class MainActivity : AppCompatActivity() {

    private lateinit var pusat: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
    }
    
    private fun Awal(){
        pasangInset(findViewById<LinearLayout>(R.id.root_utama))
        pasangInset(findViewById<LinearLayout>(R.id.root_drawer))
        AturStatusBar()
        AturTema()
    }
    
    private fun Keluar(){KeluarApp(this).mulai()}
    
    private fun pasangInset(view: View) {
        val kiri = view.paddingLeft
        val atas = view.paddingTop
        val kanan = view.paddingRight
        val bawah = view.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets( WindowInsetsCompat.Type.systemBars() )
            v.setPadding(
                kiri + bars.left, atas + bars.top, kanan + bars.right, bawah + bars.bottom
            )
            insets
        }
        
        ViewCompat.requestApplyInsets(view)
    }
    
    private fun GantiTema(){
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        
        AturStatusBar()
    }
    
    private fun AturStatusBar(){
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        window.statusBarColor = getColor(rosh.lib.R.color.bg)
        window.navigationBarColor = getColor(rosh.lib.R.color.bg)
        WindowCompat.getInsetsController( window, window.decorView
        ).isAppearanceLightStatusBars = !dark
    }
    
    private fun AturTema(){
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val gt = findViewById<ImageView>(R.id.gambar_tema)
        val nt = findViewById<TextView>(R.id.nama_tema)
        
            if (dark){
                gt.setImageResource(rosh.lib.R.drawable.bulan)
                nt.text = "Dark"
            }else{
                gt.setImageResource(rosh.lib.R.drawable.matahari)
                nt.text = "Light"
            }
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
        
        Klik(findViewById<ImageView>(R.id.nav)).sekali{
            pusat.openDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<ImageView>(R.id.tutup)).sekali{
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<ImageView>(R.id.keluar)).sekali{
            Keluar()
        }
        
        Klik(findViewById<LinearLayout>(R.id.tema)).sekali{
            GantiTema()
        }
        
        Klik(findViewById<ImageView>(R.id.git)).sekali {
            val link = "https://github.com/rosh-brid/RoshAll"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        }
        
        Klik(findViewById<LinearLayout>(R.id.terminal)).sekali{
            startActivity(Intent(this, rosh.terminal.Terminal::class.java))
        }
        
        Klik(findViewById<LinearLayout>(R.id.berkas)).sekali{
            startActivity(Intent(this, rosh.berkas.Berkas::class.java))
        }
        
        Klik(findViewById<LinearLayout>(R.id.browser)).sekali{
            startActivity(Intent(this, rosh.browser.Browser::class.java))
        }
        
        Klik(findViewById<LinearLayout>(R.id.code)).sekali{
            startActivity(Intent(this, rosh.code.Code::class.java))
        }
    }
}