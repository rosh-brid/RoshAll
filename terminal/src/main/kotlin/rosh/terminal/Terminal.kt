package rosh.terminal

import rosh.lib.exec.ShellCustom

import android.content.pm.PackageManager
import android.os.*
import android.widget.*
import android.content.*
import android.view.*
import android.app.AlertDialog
import android.Manifest

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.ContextCompat

class Terminal : AppCompatActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.terminal)
        
        Awal()
        Tombol()
    }
    
    private fun PasangId(){}
    
    private fun Awal(){}
    
    private fun Keluar(){}
    
    private fun Tombol(){}
    
}