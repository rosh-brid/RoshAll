package rosh.browser

import lib.app.RoshActivity
import lib.action.Click

import android.os.*
import android.webkit.*
import android.widget.*
import android.view.*

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.activity.OnBackPressedCallback

class Browser : RoshActivity() {

    private lateinit var pusat: DrawerLayout
    private lateinit var web: WebView
    private lateinit var prosesRoot: LinearLayout
    private lateinit var prosesAnak: LinearLayout
    private lateinit var link:TextView
    private var mesin : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView(R.layout.browser)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
        web = findViewById(R.id.web)
        prosesRoot = findViewById(R.id.proses_root)
        prosesAnak = findViewById(R.id.proses_anak)
        link = findViewById(R.id.link_sekarang)
    }
    
    private fun Awal() {
        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)

                url?.let { link.text = it }
            }
        }
        
        web.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true 
            mediaPlaybackRequiresUserGesture = false 
            loadsImagesAutomatically = true
            
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false 
            
        }
        
        web.isLongClickable = true
        web.isHapticFeedbackEnabled = true
        
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    prosesRoot.visibility = View.VISIBLE
                    val screenWidth = resources.displayMetrics.widthPixels
                    prosesAnak.layoutParams.width = (newProgress / 100.0 * screenWidth).toInt()
                    prosesAnak.requestLayout()
                } else {
                    prosesRoot.visibility = View.GONE
                }
            }
        }
    
        Jelajah("https://www.google.com")
    }

    
    private fun Keluar() {
        lib.app.Dialog(this)
            .setTitle("Exit")
            .setMessage("Exit Browser?")
            .setPositiveButton("Sure") { finish() }
            .setNegativeButton("Cancel")
            .show()
    }
    
    private fun BukaLink(){
        val item = LayoutInflater.from(this).inflate(R.layout.pop_link, null)
        val pop = PopupWindow(item, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        val tulis = item.findViewById<EditText>(R.id.tulis)
        
        tulis.setText(link.text.toString())
        
        Click(item.findViewById<TextView>(R.id.n)).one{
            pop.dismiss()
        }
        
        Click(item.findViewById<TextView>(R.id.c)).one{
            tulis.setText("")
        }
        
        Click(item.findViewById<TextView>(R.id.y)).one{
            pop.dismiss()
            Jelajah(tulis.text.toString())
        }
        
        pop.showAtLocation( window.decorView, Gravity.CENTER, 0, 0 )
    }
    
    private fun Tombol() {
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (pusat.isDrawerOpen(GravityCompat.START)) {
                        pusat.closeDrawer(GravityCompat.START)
                    } else {
                        if (web.canGoBack()) { web.goBack() } else { Keluar() }
                    }
                }
            }
        )
        
        Click(findViewById<ImageView>(R.id.nav)).one {
            pusat.openDrawer(GravityCompat.START)
        }
        
        link.setOnClickListener{
            BukaLink()
        }
        
        Click(findViewById<ImageView>(R.id.tutup)).one {
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.keluar)).one {
            Keluar()
        }
        
        Click(findViewById<ImageView>(R.id.mesin)).one {
            MesinBrowser()
        }
    }
    
    private fun Jelajah(terima:String){
        val load = web.loadUrl(terima)
        if(load == false){
           web.loadUrl("https://",terima, ".con")
        }
    }
    
    private fun MesinBrowser(){
        
    }
}
