package lib.view

import lib.R
import lib.action.Click

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

class ListFont(private val kelas: Activity) {

    private val item: View = LayoutInflater.from(kelas).inflate(R.layout.pop_list_font, null)
    private val pop: PopupWindow
    
    private val warnaTeksDefault = kelas.getColor(R.color.text)

    private var terpilih: Int = R.font.bpg_banner_quadrosquare_caps_webfont

    init {
        val grid = item.findViewById<GridLayout>(R.id.grid)
        grid.removeAllViews()

        val daftarFont = R.font::class.java.fields
            .filter { it.type == Int::class.javaPrimitiveType }
            .sortedBy { it.name }

        for (font in daftarFont) {
            val fontId = font.getInt(null)

            val teks = TextView(kelas).apply {
                text = font.name
                textSize = 16f
                setPadding(24, 16, 24, 16)

                try {
                    typeface = ResourcesCompat.getFont(kelas, fontId)
                } catch (e: Exception) {
                    e.printStackTrace() 
                }

                if (fontId == terpilih) {
                    setTextColor(kelas.getColor(R.color.parent))
                } else {
                    setTextColor(warnaTeksDefault)
                }
            }

            val params = GridLayout.LayoutParams().apply {
                width = GridLayout.LayoutParams.MATCH_PARENT
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(2, 2, 2, 2)
            }

            Click(teks).one {
                terpilih = fontId

                for (i in 0 until grid.childCount) {
                    val child = grid.getChildAt(i) as? TextView
                    child?.let {
                        if (it == teks) {
                            it.setTextColor(kelas.getColor(R.color.parent)) 
                        } else {
                            it.setTextColor(warnaTeksDefault) 
                        }
                    }
                }
            }

            grid.addView(teks, params)
        }

        pop = PopupWindow(
            item,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        val btnBatal = item.findViewById<TextView>(R.id.n)
        Click(btnBatal).one {
            pop.dismiss()
        }
    }

    fun setPositiveButton(terima: String? = null, aksi: () -> Unit) {
        val btnTerima = item.findViewById<TextView>(R.id.y)

        if (terima != null) {
            btnTerima.text = terima
        }

        Click(btnTerima).one {
            aksi()
            pop.dismiss()
        }
    }

    fun getSelectedFont(): Int {
        return terpilih
    }

    fun show() {
        pop.showAtLocation(
            kelas.window.decorView,
            Gravity.CENTER,
            0,
            0
        )
    }
}
