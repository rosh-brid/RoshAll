package lib.view

import lib.R
import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.ImageView
import java.io.File
import com.bumptech.glide.Glide

class IconFile(private val kelas: Activity) {

    private var bipmap: ImageView? = null

    fun setBipmap(terima: ImageView?) {
        bipmap = terima
    }

    fun setFile(terima: File): Int {
        return setExtension(terima.extension, terima, null)
    }

    fun setUri(terima: Uri): Int {
        val nama = when (terima.scheme) {
            "file" -> {
                terima.path?.let {
                    File(it).name
                }
            }
            "content" -> {
                var hasil: String? = null
                kelas.contentResolver.query(
                    terima, arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        hasil = cursor.getString(
                            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                        )
                    }
                }
                hasil
            }
            else -> null
        }

        val extension = nama?.substringAfterLast('.', "") ?: ""
        return setExtension(extension, null, terima)
    }

    private fun setExtension(extension: String, file: File?, uri: Uri?): Int {
        return when (extension.lowercase()) {
            "kt" -> R.drawable.file_kotlin
            "py", "pyc" -> R.drawable.file_python
            "html", "htm" -> R.drawable.file_html
            "css" -> R.drawable.file_css
            "tar", "rar", "gz", "xz", "jar", "zip", "pac" -> R.drawable.file_zip
            "gradle", "gradlew" -> R.drawable.file_gradle
            "kts" -> R.drawable.file_gradle_kts
            "java" -> R.drawable.file_java
            "json" -> R.drawable.file_json
            "xml" -> R.drawable.file_xml
            "properties", "pro", "toml" -> R.drawable.file_properties
            "s", "cs" -> R.drawable.file_c_sharp 
            "cpp", "c" -> R.drawable.file_cpp
            "so" -> R.drawable.file_binary
            "js" -> R.drawable.file_nodejs
            "go" -> R.drawable.file_golang
            "txt" -> R.drawable.file_text
            "apk" -> R.drawable.file_android
            "pdf" -> R.drawable.file_pdf
            "doc", "docx" -> R.drawable.file_doc
            "jpg", "jpeg", "gif", "png", "webp" -> ikonFoto(file, uri)
            else -> R.drawable.file_unknown
        }
    }

    private fun ikonFoto(file: File?, uri: Uri?): Int {
        val imageView = bipmap

        if (imageView != null) {
            val target = file ?: uri
            
            if (target != null) {
                Glide.with(kelas)
                    .load(target)
                    .placeholder(R.drawable.file_image)
                    .error(R.drawable.file_image_eror)
                    .centerCrop()
                    .into(imageView)
            }
            
            return 0 
        } else {
            return R.drawable.file_image
        }
    }
}
