package rosh.gambar

import lib.app.RoshActivity
import lib.action.Click

import android.os.*
import android.widget.*
import android.net.Uri

import java.io.File

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*

class Gambar : RoshActivity(){

    private lateinit var pusat:DrawerLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setView(R.layout.gambar)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
    }
    
    private fun Awal(){
    
    }
    
    private fun Keluar(){
        lib.app.Dialog(this)
            .setMessage("you will exit Images")
            .setTitle("Exit")
            .setPositiveButton("Sure") { finish() }
            .setNegativeButton("Cancel")
            .show()
    }
    
    private fun Tombol(){
        Click(findViewById<ImageView>(R.id.nav)).one{
            pusat.openDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.tutup)).one{
            pusat.closeDrawer(GravityCompat.START)
        }
        
        Click(findViewById<ImageView>(R.id.keluar)).one{
            Keluar()
        }
    }
    
    private fun MuatGaleri(file:List<File>?, uri:List<Uri>?){
        if(file != null){
            
        }
        else{
            if(uri != null){
            
            }else{
            
            }
        }
    }
}
