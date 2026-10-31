package rosh.berkas

import lib.action.Click
import lib.app.RoshActivity

import android.os.*
import android.view.*
import android.widget.*
import android.net.*
import android.content.*
import android.provider.Settings

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.activity.OnBackPressedCallback
import androidx.documentfile.provider.DocumentFile
import androidx.activity.result.contract.ActivityResultContracts

import java.io.File

class Berkas : RoshActivity(){
    
    private lateinit var pusat: DrawerLayout
    private lateinit var jalur: TextView
    private var menungguIzin = false
    private val pemilihFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flag)
            
            val docRoot = DocumentFile.fromTreeUri(this, uri)
            Segarkan(null, docRoot)
        } else {
            Toast.makeText(this, "Akses dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }
    private var fileSaatIni: File? = null
    private var docSaatIni: DocumentFile? = null
    
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setView(R.layout.berkas)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        jalur = findViewById(R.id.jalur)
    }

    private fun Awal(){
        if(BacaIzin()){
        val internal = Environment.getExternalStorageDirectory()
        Segarkan(internal, null)
    }else{
        menungguIzin = true
        MintaIzin()
    }
    }

    private fun Segarkan(file: File?, doc: DocumentFile?){
        fileSaatIni = file
        docSaatIni = doc
        
        if(file != null){
            AturGrid(file, null)
            jalur.text = file.absolutePath
        }else if(doc != null){
            AturGrid(null, doc)
            jalur.text = DapatkanJalurUri(docSaatIni)
        }else{ 
            Toast.makeText( this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT ).show()
        }
    }
    
    override fun onResume(){
        super.onResume()

        if(menungguIzin && BacaIzin()){
            menungguIzin = false

            val internal = Environment.getExternalStorageDirectory()
            Segarkan(internal, null)
        }
    }
    
    private fun BacaIzin(): Boolean{
        val sdk = Build.VERSION.SDK_INT

        return if(sdk >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            checkSelfPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun MintaIzin(){
        val sdk = Build.VERSION.SDK_INT
        if(sdk >= Build.VERSION_CODES.R){
            try{
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }catch(e: Exception){
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                )
                startActivity(intent)
            }
        }else{
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 100
            )
        }
    }

    private fun AturGrid(file: File?, doc: DocumentFile?){
        val grid = findViewById<lib.widget.RoshGrid>(R.id.grid)

        grid.removeAllViews()

        if(file != null){
            val list = file.listFiles() ?: return

            for(isi in list){
                val item = LayoutInflater.from(this).inflate(R.layout.item_berkas, grid, false)
                val nama = item.findViewById<TextView>(R.id.nama)
                val gambar = item.findViewById<ImageView>(R.id.gambar)
                val centang = item.findViewById<TextView>(R.id.centang)
                val ikon = lib.view.IconFile(this)
                
                centang.visibility = View.GONE
                
                gambar.setImageResource(
                    if(isi.isDirectory){lib.R.drawable.folder}
                    else{
                        ikon.setBipmap(gambar)
                        ikon.setFile(isi)
                    }
                )
                nama.text = isi.name
                
                Click(item).one{
                    if(isi.isDirectory){
                        Segarkan(isi, null)
                    }else{
                        BukaFile(null, isi)
                    }
                }
                
                grid.addView(item)
            }
        }
        else if(doc != null){
            val list = doc.listFiles()
            for(isi in list){
                val item = LayoutInflater.from(this).inflate(R.layout.item_berkas, grid, false)
                val nama = item.findViewById<TextView>(R.id.nama)
                val gambar = item.findViewById<ImageView>(R.id.gambar)
                val centang = item.findViewById<TextView>(R.id.centang)
                val ikon = lib.view.IconFile(this)
                
                nama.text = isi.name
                gambar.setImageResource(
                    if(isi.isDirectory){lib.R.drawable.folder}
                    else{
                        ikon.setBipmap(gambar)
                        ikon.setUri(isi.uri)
                    }
                )
                centang.visibility = View.GONE
                
                Click(item).one{
                    if(isi.isDirectory){
                        Segarkan(null, isi)
                    }else{
                        BukaFile(isi.uri, null)
                    }
                }
                
                grid.addView(item)
            }
        }
    }
    
    private fun Keluar(){
        lib.app.Dialog(this)
            .setTitle("Warning")
            .setMessage("Exit explorer")
            .setPositiveButton("sure"){finish()}
            .setNegativeButton("cencel")
            .show()
    }
    
    private fun DapatkanJalurUri(doc: DocumentFile?): String {
        if (doc == null) return "/"
        
        var jalurUri = ""
        var saatIni = doc
        
        while (saatIni != null) {
            val nama = saatIni.name ?: "Unknown"
            val namaBersih = if (nama.contains(":")) nama.substringAfter(":") else nama
            jalurUri = "/$namaBersih$jalurUri"
            saatIni = saatIni.parentFile
        }
        
        return if (jalurUri.isEmpty()) "/" else jalurUri
    }
    
    private fun BuatBaru() {
        val baru = findViewById<ImageView>(R.id.baru)
        val item = LayoutInflater.from(this).inflate(R.layout.item_buat_baru, null)
        val pop = PopupWindow( item, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true )
        
        Click(item.findViewById<LinearLayout>(R.id.folder)).one{
            pop.dismiss()
            Buat(false)
        }
        
        Click(item.findViewById<LinearLayout>(R.id.file)).one{
            pop.dismiss()
            Buat(true)
        }
        
        pop.showAsDropDown(baru)
    }
    
    private fun Buat(terima: Boolean) {
        val item = LayoutInflater.from(this).inflate(R.layout.item_tulis_nama, null)
        val pop = PopupWindow( 
            item, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, true
        )
        val tulis = item.findViewById<EditText>(R.id.tulis)
        val inti = item.findViewById<LinearLayout>(R.id.inti)
        val anak = item.findViewById<LinearLayout>(R.id.anak)
            
        anak.setOnClickListener{}
        inti.setOnClickListener{pop.dismiss()}
        
        Click(item.findViewById<TextView>(R.id.n)).one{pop.dismiss()}
        
        Click(item.findViewById<TextView>(R.id.y)).one{
            val namaBaru = tulis.text.toString()
            if(namaBaru.isEmpty()){
                Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }else{
                try {
                    if(fileSaatIni != null){
                        val target = File(fileSaatIni, namaBaru)
                        if(terima){
                            target.createNewFile()
                        }else{
                            target.mkdirs()
                        }
                        pop.dismiss()
                        Segarkan(fileSaatIni, null)
                    }
                    else if(docSaatIni != null){
                        if(terima){
                            docSaatIni!!.createFile("*/*", namaBaru)
                        }else{
                            docSaatIni!!.createDirectory(namaBaru)
                        }
                        pop.dismiss()
                        Segarkan(null, docSaatIni)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
            
        pop.showAtLocation( this.window.decorView, Gravity.CENTER, 0, 0 )
    }
    
    private fun Tombol(){
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(pusat.isDrawerOpen(GravityCompat.START)){
                        pusat.closeDrawer(GravityCompat.START)
                    }else{
                        if (fileSaatIni != null) {
                            val parent = fileSaatIni!!.parentFile
                            if (parent == null || fileSaatIni!!.absolutePath == "/") {
                                Keluar()
                            } else {
                                Segarkan(parent, null)
                            }
                        } 
                        else if (docSaatIni != null) {
                            val parentDoc = docSaatIni!!.parentFile
                            if (parentDoc == null) {
                                Keluar()
                            } else {
                                Segarkan(null, parentDoc)
                            }
                        } 
                        else {
                            Keluar()
                        }
                    }
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
        
        Click(findViewById<LinearLayout>(R.id.memo_app)).one{
            pusat.closeDrawer(GravityCompat.START)
            Segarkan(filesDir, null)
        }
        
        Click(findViewById<LinearLayout>(R.id.app_internal)).one{
            pusat.closeDrawer(GravityCompat.START)
            Segarkan(getExternalFilesDir(null), null)
        }
        
        Click(findViewById<LinearLayout>(R.id.memo_internal)).one{
            pusat.closeDrawer(GravityCompat.START)
            Segarkan(Environment.getExternalStorageDirectory(), null)
        }
        
        Click(findViewById<LinearLayout>(R.id.memo_lib)).one{
            pusat.closeDrawer(GravityCompat.START)
            Segarkan(File(applicationInfo.nativeLibraryDir), null)
        }
        
        Click(findViewById<LinearLayout>(R.id.memo_sd)).one{
            pusat.closeDrawer(GravityCompat.START)
            Segarkan(Environment.getExternalStorageDirectory(), null)
        }
        
        Click(findViewById<LinearLayout>(R.id.from_intent)).one{
            pusat.closeDrawer(GravityCompat.START)
            pemilihFolder.launch(null) 
        }
        
        Click(findViewById<LinearLayout>(R.id.usb)).one{
            pusat.closeDrawer(GravityCompat.START)
            pemilihFolder.launch(null)
        }
        
        Click(findViewById<ImageView>(R.id.baru)).one{
            BuatBaru()
        }
        
        Click(findViewById<ImageView>(R.id.urut)).one{
            
        }
        
        Click(findViewById<ImageView>(R.id.cari)).one{
            
        }
    }
    
    private fun BukaFile(uri:Uri?, file:File?){
        val item = LayoutInflater.from(this).inflate(R.layout.pop_buka_file, null)
        val pop = PopupWindow(
            item, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, true )
            
        Click(item.findViewById<TextView>(R.id.n)).one{
            pop.dismiss()
        }
        
        Click(item.findViewById<LinearLayout>(R.id.editor)).one{
        
        }
        
        Click(item.findViewById<LinearLayout>(R.id.gambar)).one{
        
        }
        
        Click(item.findViewById<LinearLayout>(R.id.video)).one{
        
        }
        
        Click(item.findViewById<LinearLayout>(R.id.musik)).one{
        
        }
        
        Click(item.findViewById<LinearLayout>(R.id.more)).one{
        
        }
        
        pop.showAtLocation( window.decorView, Gravity.CENTER, 0, 0 )
    }
}
