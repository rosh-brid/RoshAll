package rosh.code

import lib.app.RoshActivity
import lib.widget.FolderView
import lib.action.Click

import android.os.*
import android.widget.*
import android.graphics.*
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts

class Code : RoshActivity() {

    private lateinit var pusat: DrawerLayout
    private lateinit var editor: lib.widget.codeeditor.CodeEditor
    private lateinit var tempatMenu: LinearLayout
    private lateinit var menuFolder:ImageView
    private lateinit var menuPlugin:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView(R.layout.code)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
        editor = findViewById(R.id.editor)
        tempatMenu = findViewById(R.id.tempat_menu)
        menuFolder = findViewById(R.id.menu_folder)
        menuPlugin = findViewById (R.id.menu_plugin)
    }
    
    private fun Awal() {
       
    }
    
    private fun Keluar() {
        lib.app.Dialog(this)
            .setMessage("you will exit code")
            .setTitle("Exit")
            .setPositiveButton("Sure") { finish() }
            .setNegativeButton("Cancel")
            .show()
    }
    
    private fun Tombol() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pusat.isDrawerOpen(GravityCompat.START)) {
                    pusat.closeDrawer(GravityCompat.START)
                } else {
                    Keluar()
                }
            }
        })
        
        Click(findViewById<ImageView>(R.id.keluar)).one {
            Keluar()
        }
        
        Click(findViewById<ImageView>(R.id.nav)).one {
            pusat.openDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.tutup)).one {
            pusat.closeDrawer(GravityCompat.START)
        }
    }
    
    private fun TerapkanMenu(terima:Int){
       // tempatMenu.removeAllViews()
       // val fm = LayoutInflater.from(this).inflate(R.layout.item_menu_folder, tempatMenu, false)
        //val pm = LayoutInflater.from(this).inflate(R.layout.item_menu_plugin, tempatMenu, false)
        //when(terima){
            //1 -> tempatMenu.addView(fm)
            //2 -> tempatMenu.addView(pm)
        //}
    }
}
