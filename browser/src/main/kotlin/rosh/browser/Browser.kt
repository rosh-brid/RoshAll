package rosh.browser

import rosh.lib.aksi.Klik
import rosh.lib.os.ui.Inset

import android.os.*
import android.webkit.*
import android.widget.*
import android.graphics.Bitmap

import androidx.appcompat.app.*
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback

class Browser : AppCompatActivity(){

    private lateinit var pusat: DrawerLayout
    private lateinit var web: WebView
    private lateinit var link: EditText

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.browser)

        PasangId()
        Awal()
        Tombol()
    }

    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        web = findViewById(R.id.web)
        link = findViewById(R.id.link)
    }

    private fun Tombol(){
    onBackPressedDispatcher.addCallback(this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(pusat.isDrawerOpen(GravityCompat.START)){
                    pusat.closeDrawer(GravityCompat.START)
                }else if(web.canGoBack()){ web.goBack() }
                    else{ finish() }
                }
            }
        )
        
        Klik(findViewById<ImageView>(R.id.cari)).sekali{
            val baca = link.text.toString()
            Jelajahi(baca)
        }
        
        Klik(findViewById<ImageView>(R.id.nav)).sekali{
            pusat.openDrawer(GravityCompat.START)
        }
    }

    private fun Awal(){
        val inset = Inset()
        inset.pasangInset(findViewById<LinearLayout>(R.id.root_utama))
        inset.pasangInset(findViewById<LinearLayout>(R.id.root_drawer))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true         
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.webViewClient = WebViewClient()             

        Jelajahi("https://google.com")
    }
    
    private fun Jelajahi(url:String){
        web.loadUrl(url)
    }
}