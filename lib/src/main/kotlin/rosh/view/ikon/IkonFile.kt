package rosh.view.ikon

import rosh.lib.R

import java.io.File

import android.app.Activity
import android.widget.ImageView

import com.bumptech.glide.Glide

class IkonFile(private val kelas:Activity) {

    private var tipeImage : ImageView? = null

    fun Tipe(terima : File):Int{
        return when(terima.extension.lowercase()){
            "py","pyc" -> rosh.lib.R.drawable.file_python
            "js" -> rosh.lib.R.drawable.file_js
            "c","cpp","h" -> rosh.lib.R.drawable.file_cpp
            "kt" -> rosh.lib.R.drawable.file_kotlin
            "java" -> rosh.lib.R.drawable.file_java
            "kts","gradle" -> rosh.lib.R.drawable.file_gradle
            "jpg","png","webp","jpeg" -> {
                muatGlide(terima)
                rosh.lib.R.drawable.gambar}
            "3gp","mp4" -> {
                muatGlide(terima)
                rosh.lib.R.drawable.video}
            "mp3","wav" -> rosh.lib.R.drawable.musik
            "html" -> rosh.lib.R.drawable.file_html
            "css" -> rosh.lib.R.drawable.file_css
            "php" -> rosh.lib.R.drawable.file_php
            "json" -> rosh.lib.R.drawable.file_json
            "dart" -> rosh.lib.R.drawable.file_flutter
            "cs" -> rosh.lib.R.drawable.file_c_sharp
            "apk" -> rosh.lib.R.drawable.android
            "so","iso" -> rosh.lib.R.drawable.file_binary
            "xz","gz","zip","tar","rar","pac" -> rosh.lib.R.drawable.file_archive
            else -> rosh.lib.R.drawable.file
        }
    }
    
     fun setGlide(letak:ImageView?){
        tipeImage = letak
    }
    
    private fun muatGlide(terima: File) {

        val gambar = tipeImage ?: return

        Glide.with(kelas)
            .load(terima)
            .into(gambar)
    }
}
