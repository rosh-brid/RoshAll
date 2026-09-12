package rosh.berkas.pop

import rosh.berkas.R

import android.view.*
import android.app.Activity
import android.app.AlertDialog

class KeluarApp(private val kelas:Activity) {
    fun mulai(){
        AlertDialog.Builder(kelas)
            .setTitle("Exit")
            .setMessage("You will exit")
            .setPositiveButton("Sure"){_,_-> kelas.finish()}
            .setNegativeButton("Cencel", null)
            .show()
    }
}
