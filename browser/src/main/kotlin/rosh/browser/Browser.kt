package rosh.browser

import android.os.*
import android.webkit.*
import android.widget.*
import android.graphics.Bitmap

import androidx.appcompat.app.*
import androidx.drawerlayout.widget.DrawerLayout

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

    private fun Tombol(){}

    private fun Awal(){
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true          // banyak situs butuh ini (mis. Google)
        web.settings.loadWithOverviewMode = true
        web.settings.useWideViewPort = true
        web.webViewClient = WebViewClient()             // penting!

        web.loadUrl("https://www.google.com")
    }
}