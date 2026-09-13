package rosh.code

import android.os.*

import java.io.File

import androidx.appcompat.app.*

class Code : AppCompatActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code)
        
        Awal()
    }
    
    private fun Awal(){
        val v = findViewById<rosh.lib.widget.FolderView>(R.id.view)
        v.setDir(File(filesDir, "../"))
    }
}
