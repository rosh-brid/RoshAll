package rosh.terminal

import lib.app.RoshActivity

import android.os.*
import android.widget.*
import android.view.*

class Terminal : RoshActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setView(R.layout.terminal)
        
    }
}
