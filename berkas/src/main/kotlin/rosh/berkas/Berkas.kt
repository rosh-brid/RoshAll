package rosh.berkas

import rosh.lib.aksi.Klik
import rosh.view.ikon.IkonFile

import android.os.*
import android.widget.*
import android.content.*
import android.view.*

import java.io.*

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback

class Berkas : AppCompatActivity(){

    private lateinit var pusat : DrawerLayout
    private lateinit var jalur : TextView
    private val fileTerpilih = mutableListOf<File>()
    private var balonView: ImageView? = null
    private var alatPop: PopupWindow? = null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.berkas)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        jalur = findViewById(R.id.jalur)
    }
    
    private fun Awal(){
        pasangInset(findViewById<LinearLayout>(R.id.root_utama))
        pasangInset(findViewById<LinearLayout>(R.id.root_drawer))
        Segarkan(filesDir)
    }
    
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
    
    private fun Keluar(){
        rosh.berkas.pop.KeluarApp(this).mulai()
    }
    
    private fun Tombol(){
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(pusat.isDrawerOpen(GravityCompat.START)){
                        pusat.closeDrawer(GravityCompat.START)
                    }else{
                        val s = jalur.text.toString()
                        if(s != "/"){Segarkan(File(s).parentFile)}
                        else{Keluar()}
                    }
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
        
        Klik(findViewById<LinearLayout>(R.id.memo_app)).sekali{
            Segarkan(filesDir)
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<LinearLayout>(R.id.memo_apk)).sekali{
            Segarkan(Environment.getExternalStorageDirectory())
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<LinearLayout>(R.id.memo_sd)).sekali {
            val internal = Environment.getExternalStorageDirectory()
            val t = getExternalFilesDirs(null)

            for (isi in t) {
                if (isi == null) continue
                val root = File(isi.absolutePath.substringBefore("/Android/") )
                if (root.absolutePath != internal.absolutePath) { Segarkan(root) }
            }
            pusat.closeDrawer(GravityCompat.START)
        }
        
        val tUsb = findViewById<LinearLayout>(R.id.memo_usb)
        tUsb.visibility = View.GONE
        Klik(tUsb).sekali{
            Segarkan(filesDir)
            pusat.closeDrawer(GravityCompat.START)
        }
    }
    
    private fun Segarkan(terima:File){
        jalur.text = terima.absolutePath
        MuatFolder(terima)
    }
    
    private fun MuatTerpilih() {
        val viewPop = LayoutInflater.from(this).inflate(R.layout.pop_file_terpilih, null)
        val pop = PopupWindow( viewPop, LinearLayout.LayoutParams.MATCH_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT, true )
        
        val grid = viewPop.findViewById<GridLayout>(R.id.grid)
        val tutup = viewPop.findViewById<TextView>(R.id.n)
        val bersih = viewPop.findViewById<TextView>(R.id.bersih)
        grid.removeAllViews()
        val IF = IkonFile(this)
        
        for (isi in fileTerpilih.toList()) {
            val item = LayoutInflater.from(this).inflate(R.layout.item_berkas_hor, grid, false)
            val g = item.findViewById<ImageView>(R.id.gambar)
            val n = item.findViewById<TextView>(R.id.nama)
            val c = item.findViewById<TextView>(R.id.clear)
            
            n.text = isi.name
            IF.setGlide(g)
            g.setImageResource(
                if (isi.isDirectory) { rosh.lib.R.drawable.folder } 
                else { IF.Tipe(isi) }
            )
            
            Klik(c).sekali {
                grid.removeView(item)
                fileTerpilih.remove(isi)
                muatBalon()
                Segarkan(File(jalur.text.toString()))
            }
            
            grid.addView(item)
        }
        
        Klik(tutup).sekali { pop.dismiss() }
        
        Klik(bersih).sekali {
            fileTerpilih.clear()
            muatBalon()
            Segarkan(File(jalur.text.toString()))
            pop.dismiss()
        }
        
        pop.showAtLocation(pusat, Gravity.CENTER, 0, 0)
    }
    
    private fun muatBalon() {
        AlatTerpilih()
        if (fileTerpilih.isEmpty()) {
            balonView?.let { 
                (it.parent as? ViewGroup)?.removeView(it) 
            }
            balonView = null
            return
        }

        if (balonView == null) {
            val ukuran = (30 * resources.displayMetrics.density).toInt()
            val margin = (30 * resources.displayMetrics.density).toInt()

            balonView = ImageView(this).apply {
                setImageResource(rosh.lib.R.drawable.file_terpilih) 
                elevation = 30f 
                
                layoutParams = FrameLayout.LayoutParams(ukuran, ukuran).apply {
                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                    rightMargin = margin
                }
            }

            var awalX = 0f
            var awalY = 0f
            var sentuhX = 0f
            var sentuhY = 0f

            balonView!!.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        awalX = view.x
                        awalY = view.y
                        sentuhX = event.rawX
                        sentuhY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val geserX = event.rawX - sentuhX
                        val geserY = event.rawY - sentuhY
                        
                        view.x = awalX + geserX
                        view.y = awalY + geserY
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val batasKlik = 10f
                        val selisihX = Math.abs(event.rawX - sentuhX)
                        val selisihY = Math.abs(event.rawY - sentuhY)
                        
                        if (selisihX < batasKlik && selisihY < batasKlik) {
                            MuatTerpilih() 
                        }
                        true
                    }
                    else -> false
                }
            }

            val rootView = findViewById<FrameLayout>(android.R.id.content)
            rootView.addView(balonView)
        }
    }
    
    private fun MuatFolder(terima:File){
        val grid = findViewById<GridLayout>(R.id.tempat_folder)
        val i = terima.listFiles() ?: return
        grid.removeAllViews()
        for(isi in i){
            val item = LayoutInflater.from(this).inflate(R.layout.item_berkas_ver,grid, false)
            val nama = item.findViewById<TextView>(R.id.nama)
            val gambar = item.findViewById<ImageView>(R.id.gambar)
            val centang = item.findViewById<TextView>(R.id.centang)
            val IF = IkonFile(this)
            
            centang.visibility = if (isi in fileTerpilih) View.VISIBLE else View.GONE
            nama.text = isi.name
            IF.setGlide(gambar)
            gambar.setImageResource(
                if(isi.isDirectory){rosh.lib.R.drawable.folder}
                else{IF.Tipe(isi)}
            )
            
            Klik(item).sekali{
                if(isi.isDirectory){Segarkan(isi)}
                else{BukaFile(isi)}
            }
            
            Klik(item).lama{
                AturTerpilih(isi)
            }
            
            grid.addView(item)
        }
    }
    
    private fun BukaFile(terima:File){
        Toast.makeText(this, terima.name, Toast.LENGTH_SHORT).show()
    }
    
    private fun AturTerpilih(terima: File){
        if(terima in fileTerpilih){ fileTerpilih.remove(terima) }
        else{ fileTerpilih.add(terima) }
        muatBalon()
        Segarkan(File(jalur.text.toString()))
    }
    
    private fun AlatTerpilih(){
        if (fileTerpilih.isEmpty()) {
            alatPop?.dismiss()
            alatPop = null
            return
        }

        if (alatPop == null) {
            val item = LayoutInflater.from(this).inflate(R.layout.item_alat_terpilih, pusat, false)
            alatPop = PopupWindow( item, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, false ).apply { elevation = 30f }

            val salin = item.findViewById<ImageView>(R.id.salin)
            val potong = item.findViewById<ImageView>(R.id.potong)
            val tempel = item.findViewById<ImageView>(R.id.tempel)
            val rename = item.findViewById<ImageView>(R.id.rename)
            val hapus = item.findViewById<ImageView>(R.id.hapus)
            val lainya = item.findViewById<ImageView>(R.id.more)
            
        }

        if (alatPop?.isShowing != true) { alatPop?.showAtLocation( pusat, Gravity.BOTTOM, 0, 0 ) }
    }
}