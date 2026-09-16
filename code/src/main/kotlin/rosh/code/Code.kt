package rosh.code

import rosh.code.lib.Plugin

import rosh.lib.aksi.Klik
import rosh.lib.os.ui.Inset
import rosh.view.ikon.IkonFile
import rosh.lib.widget.FolderView

import android.os.*
import android.widget.*
import android.content.res.Configuration
import android.view.*
import android.content.*
import android.net.Uri
import android.provider.*

import java.io.File

import androidx.appcompat.app.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts

import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentListener
import io.github.rosemoe.sora.widget.SymbolPairMatch
import io.github.rosemoe.sora.lang.EmptyLanguage

class Code : AppCompatActivity(){

    private lateinit var pusat: DrawerLayout 
    private lateinit var kode: CodeEditor
    private lateinit var tempatAlat: LinearLayout
    private lateinit var aksiAlat: TextView
    private lateinit var simpan: FrameLayout
    private lateinit var tanda: TextView

    private val listTarget = mutableListOf<File>()
    private val tabTeks = HashMap<String, CharSequence>()
    private val tabPosisi = HashMap<String, Int>()
    private val tabAsli = HashMap<String, String>()
    private var sedangGantiTab = false           
    
    private var folderTarget: File? = null
    private var fileTarget: File? = null

    private lateinit var prefs: SharedPreferences
    
    private val pemilihFolder = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val file = uriKeFile(uri)
            if (file != null && file.exists()) {
                folderTarget = file
                prefs.edit().putString("folder", file.absolutePath).apply()
                MuatFolder()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.code)
        
        prefs = getSharedPreferences("kode_prefs", Context.MODE_PRIVATE)
        
        val pathTersimpan = prefs.getString("folder", null)
        if (pathTersimpan != null) {
            val fileTersimpan = File(pathTersimpan)
            if (fileTersimpan.exists()) {
                folderTarget = fileTersimpan
            }
        }
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        kode = findViewById(R.id.kode)
        tempatAlat = findViewById(R.id.tempat_alat)
        aksiAlat = findViewById(R.id.aksi_alat)
        simpan = findViewById(R.id.simpan)
        tanda = findViewById(R.id.tanda)
    }
    
    private fun Awal(){
        val inset = Inset()
        inset.pasangInset(findViewById<LinearLayout>(R.id.root_utama))
        inset.pasangInset(findViewById<LinearLayout>(R.id.root_drawer))
            
        if (!Environment.isExternalStorageManager()) {
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")))
        }
        
        AturStatusBar()
        AturEditor()
        MuatFolder()
        PerbaruiSimpan()
        AturPasangan()
        TerimaIntent(intent)
    }
    
    private fun uriKeFile(uri: Uri): File? {
        return try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val path = docId.substringAfter(":")
            File(Environment.getExternalStorageDirectory(), path)
        } catch (e: Exception) { null }
    }
    
    private fun AturStatusBar(){
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        window.statusBarColor = getColor(rosh.lib.R.color.bg)
        window.navigationBarColor = getColor(rosh.lib.R.color.bg)
        WindowCompat.getInsetsController( window, window.decorView
        ).isAppearanceLightStatusBars = !dark
    }
    
    private fun MuatFolder(){
        aksiAlat.text = "Directory"
        tempatAlat.removeAllViews()
        if (folderTarget == null) {
            val item = LayoutInflater.from(this).inflate(R.layout.item_folder_kosong, tempatAlat, false)
            item.setOnClickListener {
                pemilihFolder.launch(null)
            }
            tempatAlat.addView(item)
        } else {
            val item = LayoutInflater.from(this).inflate(R.layout.item_folder_ada, tempatAlat, false)
            val list = item.findViewById<FolderView>(R.id.folder_list)
            list.setDir(folderTarget!!) 
            list.setOnFileSelectedListener { fileYangDiklik ->
                BukaFile(fileYangDiklik)
                pusat.closeDrawer(GravityCompat.START)
            }
            tempatAlat.addView(item)
        }
    }
    
    private fun MuatPlugin(){
        aksiAlat.text = "Plugins"
        tempatAlat.removeAllViews()
        val item = LayoutInflater.from(this).inflate(R.layout.item_list_plugin, tempatAlat, false)
        val grid = item.findViewById<GridLayout>(R.id.grid)
        grid.removeAllViews()
        val t = TextView(this)
        t.text = "Features Coming soon"
        grid.addView(t)
        tempatAlat.addView(item)
    }
    
    private fun BukaFile(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        
        val ekstensi = file.extension.lowercase()
        
        when (ekstensi) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "mp3", "mp4", "zip", "apk", "pdf", "dex", "class" -> {
                Toast.makeText(this, "Format file $ekstensi tidak didukung", Toast.LENGTH_SHORT).show()
            }
            else -> {
                try {
                    TerapkanFile(file)
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membaca isi file", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun TerapkanFile(terima: File) {
        val tnf = findViewById<GridLayout>(R.id.tempat_nama_file)
        val path = terima.absolutePath

        val sudah = tnf.children.firstOrNull { it.tag == path }
        if (sudah != null) {
            AktifkanTab(sudah, terima)
            return
        }

        val item = LayoutInflater.from(this).inflate(R.layout.item_tempat_nama_file, tnf, false)
        val nama = item.findViewById<TextView>(R.id.nama)
        val tutup = item.findViewById<View>(R.id.tutup)
        val gambar = item.findViewById<ImageView>(R.id.gambar)

        item.tag = path
        gambar.setImageResource(IkonFile(this).Tipe(terima))
        nama.text = terima.name

        val isiAwal = terima.readText()
        tabTeks[path] = isiAwal
        tabAsli[path] = isiAwal

        Klik(item).sekali {
            AktifkanTab(item, terima)
        }

        Klik(tutup).sekali {
            TutupTab(item, terima)
        }

        listTarget.add(terima)
        tnf.addView(item)
        PerbaruiSimpan()

        AktifkanTab(item, terima)
    }
    
    private fun AktifkanTab(item: View, file: File) {
        val pathLama = fileTarget?.absolutePath
        val pathBaru = file.absolutePath

        if (pathLama != null && pathLama != pathBaru) {
            tabTeks[pathLama] = kode.text
            tabPosisi[pathLama] = kode.cursor.left
        }

        fileTarget = file

        sedangGantiTab = true
        kode.setText(tabTeks[pathBaru] ?: file.readText())
        sedangGantiTab = false

        kode.cursor.set(tabPosisi[pathBaru] ?: 0, 0)
        PasangListenerTeks()
        CekTanda()

        val tnf = findViewById<GridLayout>(R.id.tempat_nama_file)
        for (v in tnf.children) {
            v.alpha = if (v === item) 1f else 0.5f
        }
    }

    private fun TutupTab(item: View, file: File) {
        val tnf = findViewById<GridLayout>(R.id.tempat_nama_file)
        val path = file.absolutePath
        val aktifSekarang = (fileTarget?.absolutePath == path)

        tnf.removeView(item)
        listTarget.remove(file)
        tabTeks.remove(path)
        tabPosisi.remove(path)
        tabAsli.remove(path)
        PerbaruiSimpan()

        if (!aktifSekarang) {
            CekTanda()
            return
        }

        fileTarget = null

        if (listTarget.isNotEmpty()) {
            val berikutnya = listTarget.last()
            val viewBerikutnya = tnf.children.firstOrNull { it.tag == berikutnya.absolutePath }
            if (viewBerikutnya != null) {
                AktifkanTab(viewBerikutnya, berikutnya)
                return
            }
        }

        sedangGantiTab = true
        kode.setText("")
        sedangGantiTab = false
        tanda.text = "✓"
        tanda.setTextColor(0xFF4CAF50.toInt())
    }
    
    private fun SimpanFile(){
        val file = fileTarget ?: return
        val path = file.absolutePath
        val isi = kode.text.toString()

        try {
            file.writeText(isi)
            tabTeks[path] = isi
            tabAsli[path] = isi
            CekTanda()
            Toast.makeText(this, "Tersimpan: ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun PerbaruiSimpan() {
        simpan.visibility = if (listTarget.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun PasangListenerTeks() {
        kode.text.addContentListener(object : ContentListener {
            override fun beforeReplace(content: Content) {}

            override fun afterInsert(content: Content, startLine: Int, startColumn: Int,
                                     endLine: Int, endColumn: Int,
                                     changedContent: CharSequence) {
                CekTanda()
            }

            override fun afterDelete(content: Content, startLine: Int, startColumn: Int,
                                     endLine: Int, endColumn: Int,
                                     changedContent: CharSequence) {
                CekTanda()
            }
        })
    }
    
    private fun CekTanda() {
        if (sedangGantiTab) return
        val path = fileTarget?.absolutePath ?: return

        val bersih = kode.text.toString() == tabAsli[path]
        tanda.text = if (bersih) "✓" else "✗"
        tanda.setTextColor(if (bersih) 0xFF4CAF50.toInt() else 0xFFE53935.toInt())
    }
    
    private fun uriKeFileUmum(uri: Uri): File? {
        if (uri.scheme == "file") {
            return uri.path?.let { File(it) }
        }
        var nama = "dibagikan.txt"
        try {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) nama = c.getString(idx)
        }
    } catch (_: Exception) {}

    return try {
        val target = File(cacheDir, "terima/$nama")
        target.parentFile?.mkdirs()
        contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target
        } catch (e: Exception) { null }
    }
    
    private fun TerimaIntent(i: Intent?) {
    if (i == null) return

    val uri: Uri? = i.data
        ?: if (Build.VERSION.SDK_INT >= 33)
               i.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
           else
               @Suppress("DEPRECATION")
               i.getParcelableExtra(Intent.EXTRA_STREAM)

    if (uri != null) {
        val file = uriKeFileUmum(uri)
        if (file != null && file.exists()) {
            BukaFile(file)
        } else {
            Toast.makeText(this, "Gagal menerima file", Toast.LENGTH_SHORT).show()
        }
        return
    }

    if (i.action == Intent.ACTION_SEND) {
        val teks = i.getStringExtra(Intent.EXTRA_TEXT)
        if (teks != null) {
            sedangGantiTab = true
            kode.setText(teks)
            sedangGantiTab = false
            PasangListenerTeks()
            Toast.makeText(this, "Teks diterima (belum punya file)", Toast.LENGTH_SHORT).show()
        }
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    TerimaIntent(intent)
}

    
    private fun Keluar(){
        finish()
    }
    
    private fun Tombol(){
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pusat.isDrawerOpen(GravityCompat.START)) {
                    pusat.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
        Klik(findViewById<ImageView>(R.id.tutup)).sekali{
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<ImageView>(R.id.nav)).sekali{
            pusat.openDrawer(GravityCompat.START)
        }
        
        Klik(findViewById<ImageView>(R.id.keluar)).sekali{
            Keluar()
        }
        
        Klik(findViewById<ImageView>(R.id.pilih_folder)).sekali{
            MuatFolder()
        }
        
        Klik(findViewById<ImageView>(R.id.pilih_plugin)).sekali{
            MuatPlugin()
        }
        
        Klik(simpan).sekali{
            SimpanFile()
        }
    }
    
    private fun AturEditor() {
        val bg = getColor(rosh.lib.R.color.ui_bg)
        val tulisan = getColor(rosh.lib.R.color.tulisan)
        val biru = getColor(rosh.lib.R.color.ui_biru)

        val scheme = object : EditorColorScheme() {
            override fun applyDefault() {
                super.applyDefault()

                setColor(WHOLE_BACKGROUND, bg)
                setColor(LINE_NUMBER_BACKGROUND, bg)
                setColor(TEXT_NORMAL, tulisan)
                setColor(LINE_NUMBER, tulisan)
                setColor(LINE_NUMBER_CURRENT, tulisan)
                setColor(SELECTED_TEXT_BACKGROUND, biru)
                setColor(CURRENT_LINE, 0x11FFFFFF)       
                setColor(LINE_DIVIDER, 0x33FFFFFF)
                setColor(BLOCK_LINE, 0x33FFFFFF)  
            }
        }

        kode.colorScheme = scheme 
    }
    
    private fun AturPasangan() {
    val pasangan = SymbolPairMatch()

    pasangan.putPair('(', SymbolPairMatch.SymbolPair("(", ")"))
    pasangan.putPair('{', SymbolPairMatch.SymbolPair("{", "}"))
    pasangan.putPair('[', SymbolPairMatch.SymbolPair("[", "]"))
    pasangan.putPair('"', SymbolPairMatch.SymbolPair("\"", "\""))
    pasangan.putPair('\'', SymbolPairMatch.SymbolPair("'", "'"))

    //kode.setSymbolPairMatch(pasangan)
    }

}
