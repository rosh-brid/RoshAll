package rosh.code

import rosh.lib.aksi.Klik
import rosh.lib.os.ui.Inset

import android.os.*
import android.widget.*

import java.io.File

import androidx.appcompat.app.*
import androidx.drawerlayout.widget.DrawerLayout

import io.github.rosemoe.sora.widget.CodeEditor

class Code : AppCompatActivity(){

    private lateinit var pusat: DrawerLayout 
    private lateinit var kode: CodeEditor

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
        kode = findViewById(R.id.kode)
    }
    
    private fun Awal(){
        val inset = Inset()
            inset.pasangInset(findViewById<LinearLayout>(R.id.root_utama))
            inset.pasangInset(findViewById<LinearLayout>(R.id.root_drawer))
    }
    
    private fun Tombol(){
        
    }
}
