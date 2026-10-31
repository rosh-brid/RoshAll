package rosh.video

import lib.app.RoshActivity

import android.os.*
import android.widget.*

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*
import androidx.activity.OnBackPressedCallback
import androidx.documentfile.provider.DocumentFile
import androidx.activity.result.contract.ActivityResultContracts

class Video : RoshActivity(){

    private lateinit var pusat:LinearLayout

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setView(R.layout.video)
        
        PasangId()
        Awal()
        Tombol()
    }
    
    private fun PasangId(){
        pusat = findViewById(R.id.pusat)
    }
    
    private fun Keluar(){
        lib.app.Dialog(this)
            .setTitle("Exit")
            .setMessage("Exit Video?")
            .setPositiveButton("sure"){finish()}
            .setNegativeButton("cencel")
            .show()
    }
    
    private fun Awal(){
    
    }
    
    private fun Tombol(){
        onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Keluar()
                }
            }
        )
    }
}
