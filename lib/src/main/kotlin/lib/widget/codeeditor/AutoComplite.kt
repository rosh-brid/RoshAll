package lib.widget.codeeditor

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.PopupWindow
import android.widget.TextView
import lib.R

class AutoComplite(private val c: Context) {

    private lateinit var item: View
    private lateinit var pop: PopupWindow
    private lateinit var tempat: GridLayout

    private var lokasi: View? = null

    private fun dp(nilai: Int): Int {
        return (nilai * c.resources.displayMetrics.density).toInt()
    }

    init {
        item = LayoutInflater.from(c)
            .inflate(R.layout.item_auto_complite_code, null)

        tempat = item.findViewById(R.id.grid)

        pop = PopupWindow(
            item,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dp(200),
            true
        ).apply {
            isOutsideTouchable = true
            elevation = dp(8).toFloat()
        }
    }

    fun setData(list: List<String>?) {
        tempat.removeAllViews()

        if (list.isNullOrEmpty()) {
            pop.dismiss()
            return
        }

        for (isi in list) {
            val daftar = TextView(c).apply {
                text = isi
                setTextColor(Color.WHITE)
                setPadding(
                    dp(12),
                    dp(8),
                    dp(12),
                    dp(8)
                )

                layoutParams = GridLayout.LayoutParams().apply {
                    setMargins(
                        dp(2),
                        dp(2),
                        dp(2),
                        dp(2)
                    )
                }

                isClickable = true
                isFocusable = true
            }

            tempat.addView(daftar)
        }

        val anchor = lokasi

        if (anchor != null && anchor.isAttachedToWindow) {
            if (pop.isShowing) {
                pop.update()
            } else {
                pop.showAsDropDown(anchor)
            }
        }
    }

    fun setLocation(view: View?) {
        lokasi = view
    }

    fun dismiss() {
        pop.dismiss()
    }
}