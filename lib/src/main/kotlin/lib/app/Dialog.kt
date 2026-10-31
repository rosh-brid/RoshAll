package lib.app

import lib.R
import lib.action.Click

import android.app.Activity
import android.view.*
import android.widget.*

class Dialog(private val kelas: Activity) {

    private lateinit var item: View
    private lateinit var pop: PopupWindow

    init {

        item = LayoutInflater
            .from(kelas)
            .inflate(R.layout.pop_dialog, null)

        pop = PopupWindow(
            item,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )

        val inti = item.findViewById<LinearLayout>(R.id.inti)
        val anak = item.findViewById<LinearLayout>(R.id.anak)

        inti.setOnClickListener {
            pop.dismiss()
        }

        anak.setOnClickListener {
        }

        // Default: tombol tidak digunakan
        item.findViewById<TextView>(R.id.y).visibility = View.GONE
        item.findViewById<TextView>(R.id.n).visibility = View.GONE
    }

    fun setMessage(terima: String): Dialog {

        val pesan = item.findViewById<TextView>(R.id.pesan)

        pesan.text = terima

        return this
    }

    fun setTitle(terima: String): Dialog {
        val judul = item.findViewById<TextView>(R.id.judul)
        judul.text = terima
        return this
    }

    fun setPositiveButton(
        terima: String,
        aksi: (() -> Unit)? = null
    ): Dialog {

        val y = item.findViewById<TextView>(R.id.y)

        y.text = terima
        y.visibility = View.VISIBLE

        Click(y).one {
            aksi?.invoke()
            pop.dismiss()
        }

        return this
    }

    fun setNegativeButton(
        terima: String,
        aksi: (() -> Unit)? = null
    ): Dialog {

        val n = item.findViewById<TextView>(R.id.n)

        n.text = terima
        n.visibility = View.VISIBLE

        Click(n).one {
            aksi?.invoke()
            pop.dismiss()
        }

        return this
    }

    fun show(): Dialog {
        pop.showAtLocation( kelas.window.decorView, Gravity.CENTER, 0, 0 )
        return this
    }

    fun dismiss() {
        pop.dismiss()
    }
}