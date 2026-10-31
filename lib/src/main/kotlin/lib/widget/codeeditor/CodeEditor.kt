package lib.widget.codeeditor

import lib.R

import android.content.*
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import android.widget.*
import android.text.InputType
import android.view.inputmethod.*
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.os.*

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.res.ResourcesCompat

class CodeEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val warna = Paint(Paint.ANTI_ALIAS_FLAG)
    private val warnaSeleksi = Paint(Paint.ANTI_ALIAS_FLAG)
    private val warnaHandle = Paint(Paint.ANTI_ALIAS_FLAG)
    private val globalText = context.getColor(R.color.text)

    private var latarNomor = context.getColor(R.color.pop)
    private var warnaNomor = globalText
    private var warnaEditor = globalText
    private var latarEditor = context.getColor(R.color.log)
    
    private val warnaLatarSeleksi = Color.parseColor("#6633B5E5")
    private val warnaHandleSeleksi = Color.parseColor("#FF33B5E5")

    private var kursorTerlihat = true
    private var isiEditor: String = ""
    private var posisiKursor: Int = 0
    private var posisiX: Float = 50f
    
    private var awalSeleksi: Int = -1
    private var akhirSeleksi: Int = -1
    private var sedangDrag: Boolean = false
    private var handleAktif: Int = 0
    
    private val rectHandleKiri = RectF()
    private val rectHandleKanan = RectF()
    private val ukuranHandle = 25f
    
    private var posisiSentuhTerakhir: Float = 50f
    private var posisiSentuhTerakhirY: Float = 50f
    private var posisiAwalX: Float = 0f
    private var posisiAwalY: Float = 0f
    private var sedangTap: Boolean = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private val longPressDelay = 500L
    private var longPressTerpicu: Boolean = false
    private var popupMenu: PopupWindow? = null

    private val tinggiBaris = 45f
    
    private var globalFont: Typeface = ResourcesCompat.getFont(context, R.font.arial_geo_bolditalic_webfont ) ?: Typeface.DEFAULT
    
    private var polaCommandAwal: String? = null
    private var polaCommandAkir: String? = null
    
    private val displayRect = Rect()
    private val lokasiView = IntArray(2)

    private val scroller = OverScroller(context)
    private var velocityTracker: VelocityTracker? = null

    private val kedip = ValueAnimator.ofFloat(1f, 0f).apply {
        duration = 1000
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            kursorTerlihat = it.animatedValue as Float > 0.5f
            invalidate()
        }
    }
    
    private var kodeAktif: Boolean = false
    
    private var warnaString = Color.GREEN
    private var warnaCommand = Color.MAGENTA
    private var warnaNumber = Color.CYAN
    private var warnaBoolean = Color.YELLOW
    private var warnaOperator = globalText
    private var warnaVariable = globalText
    private var warnaType = Color.BLUE
    private var warnaSelection = warnaLatarSeleksi

    private var polaOperator: List<String> = emptyList()
    private var polaVariable: List<String> = emptyList()
    private var polaType: List<String> = emptyList()

    init {
        warna.textSize = 30f
        warna.color = globalText
        warna.typeface = globalFont
        warnaSeleksi.color = warnaLatarSeleksi
        warnaHandle.color = warnaHandleSeleksi
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onDraw(kertas: Canvas) {
        super.onDraw(kertas)

        kertas.drawColor(latarEditor)
        kertas.save()
        kertas.translate(scrollX.toFloat(), 0f)
        kertas.restore()
        EditorTeks(kertas)
        warna.color = latarNomor
        kertas.drawRect( 0f, 0f, 40f, height.toFloat(), warna )
        NomorTeks(kertas)
        Highlight(kertas)
        Handle(kertas)
        Kursor(kertas)
    }

    private fun NomorTeks(kertas: Canvas) {
        val barisTeks = isiEditor.split("\n")
        warna.color = warnaNomor
        for (i in barisTeks.indices) {
            kertas.drawText((i + 1).toString(), 10f, 50f + (i * tinggiBaris), warna)
        }
    }

    private fun EditorTeks(kertas: Canvas) {
        val barisTeks = isiEditor.split("\n")
        warna.style = Paint.Style.FILL
        for (i in barisTeks.indices) {
            val teksBaris = barisTeks[i]
            val y = 50f + (i * tinggiBaris)
            if (kodeAktif) {
                GambarTeksDenganSyntaxHighlight(kertas, teksBaris, 50f, y)
            } else {
                warna.color = warnaEditor
                kertas.drawText(teksBaris, 50f, y, warna)
            }
        }
    }

    private fun GambarTeksDenganSyntaxHighlight(kertas: Canvas, teks: String, startX: Float, y: Float) {
        var x = startX
        var i = 0
        
        while (i < teks.length) {
            if (teks[i] == '"') {
                val akhirString = teks.indexOf('"', i + 1)
                val batasAkhir = if (akhirString != -1) akhirString + 1 else teks.length
                val teksString = teks.substring(i, batasAkhir)
                
                warna.color = warnaString
                kertas.drawText(teksString, x, y, warna)
                x += warna.measureText(teksString)
                i = batasAkhir
                continue
            }
            
            if (teks[i].isDigit()) {
                var batasAkhir = i
                while (batasAkhir < teks.length && teks[batasAkhir].isDigit()) {
                    batasAkhir++
                }
                val teksAngka = teks.substring(i, batasAkhir)
                
                warna.color = warnaNumber
                kertas.drawText(teksAngka, x, y, warna)
                x += warna.measureText(teksAngka)
                i = batasAkhir
                continue
            }
            
            if (teks[i].isLetter()) {
                var batasAkhir = i
                while (batasAkhir < teks.length && (teks[batasAkhir].isLetterOrDigit() || teks[batasAkhir] == '_')) {
                    batasAkhir++
                }
                val kata = teks.substring(i, batasAkhir)
                
                warna.color = when {
                    kata == "true" || kata == "false" -> warnaBoolean
                    polaType.contains(kata) -> warnaType
                    polaVariable.contains(kata) -> warnaVariable
                    else -> globalText 
                }
                kertas.drawText(kata, x, y, warna)
                x += warna.measureText(kata)
                i = batasAkhir
                continue
            }
            
            var ketemuOperator = false
            for (op in polaOperator) {
                if (teks.startsWith(op, i)) {
                    warna.color = warnaOperator
                    kertas.drawText(op, x, y, warna)
                    x += warna.measureText(op)
                    i += op.length
                    ketemuOperator = true
                    break
                }
            }
            if (ketemuOperator) continue
            
            val karakter = teks[i].toString()
            warna.color = globalText
            kertas.drawText(karakter, x, y, warna)
            x += warna.measureText(karakter)
            i++
        }
    }
    
    private fun getTinggiTerlihat(): Int {
        getWindowVisibleDisplayFrame(displayRect)
        getLocationInWindow(lokasiView)
        
        val viewBottom = lokasiView[1] + height
        val tertutupKeyboard = maxOf(0, viewBottom - displayRect.bottom)
        
        return maxOf(200, height - tertutupKeyboard)
    }



    private fun dapatkanPosisi(index: Int): Pair<Int, Int> {
        var sisa = index
        val barisTeks = isiEditor.split("\n")
        for (i in barisTeks.indices) {
            if (sisa <= barisTeks[i].length) {
                return Pair(i, sisa)
            }
            sisa -= (barisTeks[i].length + 1)
        }
        return Pair(barisTeks.lastIndex, barisTeks.last().length)
    }

    private fun Highlight(kertas: Canvas) {
        if (!AdaSeleksi()) return
        
        val mulai = minOf(awalSeleksi, akhirSeleksi)
        val selesai = maxOf(awalSeleksi, akhirSeleksi)
        
        val (barisMulai, kolomMulai) = dapatkanPosisi(mulai)
        val (barisSelesai, kolomSelesai) = dapatkanPosisi(selesai)
        val barisTeks = isiEditor.split("\n")

        for (i in barisMulai..barisSelesai) {
            val teksBaris = barisTeks[i]
            val awalX = if (i == barisMulai) 50f + warna.measureText(teksBaris.substring(0, kolomMulai)) else 50f
            val akhirX = if (i == barisSelesai) {
                50f + warna.measureText(teksBaris.substring(0, kolomSelesai))
            } else {
                50f + warna.measureText(teksBaris) + 15f
            }
            
            val yAtas = 50f + (i * tinggiBaris) - warna.textSize + 5f
            val yBawah = 50f + (i * tinggiBaris) + 8f
            kertas.drawRect(awalX, yAtas, akhirX, yBawah, warnaSeleksi)
        }
    }

    private fun Handle(kertas: Canvas) {
        if (!AdaSeleksi()) return
        
        val mulai = minOf(awalSeleksi, akhirSeleksi)
        val selesai = maxOf(awalSeleksi, akhirSeleksi)
        
        val (barisKiri, kolomKiri) = dapatkanPosisi(mulai)
        val (barisKanan, kolomKanan) = dapatkanPosisi(selesai)
        val barisTeks = isiEditor.split("\n")
        
        val xKiri = 50f + warna.measureText(barisTeks[barisKiri].substring(0, kolomKiri))
        val yKiri = 50f + (barisKiri * tinggiBaris) + 10f
        
        val xKanan = 50f + warna.measureText(barisTeks[barisKanan].substring(0, kolomKanan))
        val yKanan = 50f + (barisKanan * tinggiBaris) + 10f
        
        GambarHandle(kertas, xKiri, yKiri, true)
        GambarHandle(kertas, xKanan, yKanan, false)
    }

    private fun GambarHandle(kertas: Canvas, x: Float, y: Float, kiri: Boolean) {
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x - 5f, y + 10f)
        path.lineTo(x + 5f, y + 10f)
        path.close()
        
        val pusatY = y + 10f + ukuranHandle
        path.addCircle(x, pusatY, ukuranHandle, Path.Direction.CW)
        kertas.drawPath(path, warnaHandle)
        
        val rect = if (kiri) rectHandleKiri else rectHandleKanan
        rect.set(
            x - ukuranHandle - 15f,
            y - 15f,
            x + ukuranHandle + 15f,
            pusatY + ukuranHandle + 15f
        )
    }

    private fun Kursor(kertas: Canvas) {
        if (!kursorTerlihat || AdaSeleksi()) return
        warna.color = globalText
        
        val pos = posisiKursor.coerceIn(0, isiEditor.length)
        val (baris, kolom) = dapatkanPosisi(pos)
        val barisTeks = isiEditor.split("\n")
        
        val x = 50f + warna.measureText(barisTeks[baris].substring(0, kolom))
        val y = 50f + (baris * tinggiBaris)
        kertas.drawText("|", x, y, warna)
    }

    fun setBackgroundNumber(terima: Int?) {
        latarNomor = terima ?: context.getColor(R.color.pop)
        invalidate()
    }

    fun setNumberColor(terima: Int?) {
        warnaNomor = terima ?: globalText
        invalidate()
    }

    fun setTextGlobalColor(terima: Int?) {
        warna.color = terima ?: globalText
        invalidate()
    }

    fun setBackgroundEditor(terima: Int?) {
        latarEditor = terima ?: context.getColor(R.color.log)
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        kedip.start()
    }

    override fun onDetachedFromWindow() {
        kedip.cancel()
        popupMenu?.dismiss()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        constrainScroll()
    }
    
    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val imeVisible = WindowInsetsCompat.toWindowInsetsCompat(insets)
            .isVisible(WindowInsetsCompat.Type.ime())
        
        handler.postDelayed({
            constrainScroll() 
            if (imeVisible) {
                ensureCursorVisible()
            } else {
                invalidate()
            }
        }, 150)
        
        return super.onApplyWindowInsets(insets)
    }


    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            constrainScroll()
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun append(terima: String) {
        if (AdaSeleksi()) {
            GantiSeleksi(tekanLama = false, teksBaru = terima)
        } else {
            isiEditor = isiEditor.substring(0, posisiKursor) + 
                        terima + 
                        isiEditor.substring(posisiKursor)
            posisiKursor += terima.length
        }
        constrainScroll()
        invalidate()
        ensureCursorVisible()
    }

    fun setText(terima: String?) {
        isiEditor = terima ?: ""
        posisiKursor = isiEditor.length
        BersihkanSeleksi()
        constrainScroll()
        invalidate()
    }

    private fun Keyboard(): Boolean {
        val insets = ViewCompat.getRootWindowInsets(this) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
    }

    private fun BukaKeyboard() {
        if (!hasFocus()) {
            requestFocus()
        }
        val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        keyboard.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        handler.postDelayed({ ensureCursorVisible() }, 150)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                posisiX = event.x
                posisiAwalX = event.x
                posisiAwalY = event.y
                posisiSentuhTerakhir = event.x
                posisiSentuhTerakhirY = event.y 
                sedangTap = true
                longPressTerpicu = false  
                
                scroller.forceFinished(true)
                
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)

                if (AdaSeleksi()) {
                    if (rectHandleKiri.contains(event.x, event.y)) {
                        handleAktif = 1
                        sedangDrag = true
                        sedangTap = false
                        popupMenu?.dismiss()
                        return true
                    } else if (rectHandleKanan.contains(event.x, event.y)) {
                        handleAktif = 2
                        sedangDrag = true
                        sedangTap = false
                        popupMenu?.dismiss()
                        return true
                    }
                }
                
                popupMenu?.dismiss()
                
                val sentuhX = event.x
                val sentuhY = event.y

                longPressRunnable = Runnable {
                    longPressTerpicu = true
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                    val posisi = CariPosisiKursor(sentuhX, sentuhY)
                    PilihKataAtauKarakterTerakhir(posisi, sentuhX, sentuhY)
                    TekanLama()

                    sedangTap = false
                }
                handler.postDelayed(longPressRunnable!!, longPressDelay)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                
                if (sedangDrag) {
                    val posisiBaru = CariPosisiKursor(event.x, event.y)
                    if (handleAktif == 1) {
                        awalSeleksi = posisiBaru
                    } else if (handleAktif == 2) {
                        akhirSeleksi = posisiBaru
                    }
                    invalidate()
                    return true
                }
                
                val dx = posisiSentuhTerakhir - event.x
                val dy = posisiSentuhTerakhirY - event.y
                
                if (dx != 0f || dy != 0f) {
                    scrollBy(dx.toInt(), dy.toInt())
                    constrainScroll()
                    
                    posisiSentuhTerakhir = event.x
                    posisiSentuhTerakhirY = event.y
                    
                    val totalGeserX = Math.abs(event.x - posisiAwalX)
                    val totalGeserY = Math.abs(event.y - posisiAwalY)
                    if (sedangTap && (totalGeserX > 10f || totalGeserY > 10f)) {
                        sedangTap = false
                        longPressRunnable?.let { handler.removeCallbacks(it) }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
                
                velocityTracker?.let {
                    it.computeCurrentVelocity(1000)
                    val vx = it.xVelocity.toInt()
                    val vy = it.yVelocity.toInt()
                    if (Math.abs(vx) > 500 || Math.abs(vy) > 500) {
                        scroller.fling(
                            scrollX, scrollY, 
                            -vx, -vy, 
                            0, calculateMaxScrollX(), 
                            0, calculateMaxScrollY()
                        )
                        ViewCompat.postInvalidateOnAnimation(this)
                    }
                    it.recycle()
                }
                velocityTracker = null
                
                if (sedangDrag) {
                    sedangDrag = false
                    handleAktif = 0
                    if (AdaSeleksi()) {
                        TekanLama()
                    }
                    return true
                }
                
                    if (sedangTap && !longPressTerpicu) {
                        if (AdaSeleksi()) {
                            BersihkanSeleksi()
                        } else { 
                        posisiKursor = CariPosisiKursor(event.x, event.y)
                        invalidate()
                        
                        if (Keyboard()) {
                            ensureCursorVisible()
                        } else { 
                            BukaKeyboard() 
                        }
                    }
                }
                performClick()
                return true
            }
            
            MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
                sedangDrag = false
                handleAktif = 0
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun CariPosisiKursor(sentuhX: Float, sentuhY: Float): Int {
        val realX = sentuhX + scrollX
        val realY = sentuhY + scrollY
        
        val barisTeks = isiEditor.split("\n")
        val yTengah = realY - (50f - warna.textSize)
        var indeksBaris = (yTengah / tinggiBaris).toInt()
        
        if (indeksBaris < 0) indeksBaris = 0
        if (indeksBaris > barisTeks.lastIndex) indeksBaris = barisTeks.lastIndex

        val teksBaris = barisTeks[indeksBaris]
        val jarakX = realX - 50f
        var kolomTarget = 0

        if (jarakX > 0) {
            for (i in 1..teksBaris.length) {
                val lebar = warna.measureText(teksBaris.substring(0, i))
                if (jarakX < lebar) {
                    val sebelumnya = warna.measureText(teksBaris.substring(0, i - 1))
                    kolomTarget = if (jarakX < (sebelumnya + lebar) / 2f) i - 1 else i
                    break
                }
                if (i == teksBaris.length) kolomTarget = teksBaris.length
            }
        }

        var posisiAbsolut = 0
        for (i in 0 until indeksBaris) {
            posisiAbsolut += barisTeks[i].length + 1
        }
        return posisiAbsolut + kolomTarget
    }
    
    private fun AdaSeleksi(): Boolean {
        return awalSeleksi != -1 && akhirSeleksi != -1 && awalSeleksi != akhirSeleksi
    }
    
    private fun MulaiSeleksi(dariPosisi: Int) {
        awalSeleksi = dariPosisi
        akhirSeleksi = dariPosisi
        posisiKursor = dariPosisi
        invalidate()
    }
    
    private fun BersihkanSeleksi() {
        awalSeleksi = -1
        akhirSeleksi = -1
        invalidate()
    }
    
    private fun GantiSeleksi(tekanLama: Boolean, teksBaru: String) {
        if (!AdaSeleksi()) return
        val mulai = minOf(awalSeleksi, akhirSeleksi)
        val selesai = maxOf(awalSeleksi, akhirSeleksi)
        
        isiEditor = isiEditor.substring(0, mulai) + teksBaru + isiEditor.substring(selesai)
        posisiKursor = mulai + teksBaru.length
        BersihkanSeleksi()
        constrainScroll()
    }
    
    private fun TekanLama() {
        popupMenu?.dismiss() 
        val item = LayoutInflater.from(context).inflate(R.layout.item_long_code, null)
        
        val pop = PopupWindow(
            item, ViewGroup.LayoutParams.WRAP_CONTENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT, false 
        )
        
        pop.isOutsideTouchable = true
        pop.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupMenu = pop 
        
        pop.setTouchInterceptor { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                pop.dismiss()
                false 
            } else {
                false
            }
        }
        
        val lokasiView = IntArray(2)
        getLocationInWindow(lokasiView)
        
        val xTengah = if (AdaSeleksi()) {
            val mulai = minOf(awalSeleksi, akhirSeleksi)
            val selesai = maxOf(awalSeleksi, akhirSeleksi)
            val (barisMulai, kolomMulai) = dapatkanPosisi(mulai)
            val (barisSelesai, kolomSelesai) = dapatkanPosisi(selesai)
            val barisTeks = isiEditor.split("\n")
            
            val xKiri = 50f + warna.measureText(barisTeks[barisMulai].substring(0, kolomMulai))
            val xKanan = 50f + warna.measureText(barisTeks[barisSelesai].substring(0, kolomSelesai))
            (xKiri + xKanan) / 2f
        } else {
            posisiX
        }
        
        val posisiPatokan = if (AdaSeleksi()) minOf(awalSeleksi, akhirSeleksi) else posisiKursor
        val (barisPopup, _) = dapatkanPosisi(posisiPatokan)
        
        val yPopup = 50f + (barisPopup * tinggiBaris) - 120f 
        val finalY = if ((lokasiView[1] + yPopup) < 0) {
            lokasiView[1] + 50f + (barisPopup * tinggiBaris) + 50f 
        } else {
            lokasiView[1] + yPopup
        }
        
        pop.showAtLocation(
            this,
            Gravity.NO_GRAVITY,
            (lokasiView[0] + xTengah - 100).toInt(),  
            finalY.toInt()
        )
        
        item.findViewById<ImageView>(R.id.pilih_semua).setOnClickListener {
            PilihSemua()
            pop.dismiss()
        }
        item.findViewById<ImageView>(R.id.potong).setOnClickListener {
            Potong()
            pop.dismiss()
        }
        item.findViewById<ImageView>(R.id.salin).setOnClickListener {
            Salin()
            pop.dismiss()
        }
        item.findViewById<ImageView>(R.id.tempel).setOnClickListener {
            Tempel()
            pop.dismiss()
        }
    }

    private fun Salin() {
        val teks = if (AdaSeleksi()) {
            val mulai = minOf(awalSeleksi, akhirSeleksi)
            val selesai = maxOf(awalSeleksi, akhirSeleksi)
            isiEditor.substring(mulai, selesai)
        } else {
            isiEditor
        }
        if (teks.isEmpty()) return
        
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Kode Editor", teks)
        clipboard.setPrimaryClip(clip)
    }

    private fun Potong() {
        if (AdaSeleksi()) {
            Salin()
            GantiSeleksi(tekanLama = false, teksBaru = "")
            constrainScroll()
            invalidate()
        } else if (isiEditor.isNotEmpty()) {
            Salin()
            isiEditor = ""
            posisiKursor = 0
            constrainScroll()
            invalidate()
        }
    }

    private fun Tempel() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val teksTempel = clipData.getItemAt(0).text.toString()
                if (AdaSeleksi()) {
                    GantiSeleksi(tekanLama = false, teksBaru = teksTempel)
                } else {
                    append(teksTempel)
                }
                constrainScroll()
                invalidate()
            }
        }
    }

    private fun PilihSemua() {
        if (isiEditor.isEmpty()) return
        awalSeleksi = 0
        akhirSeleksi = isiEditor.length
        posisiKursor = isiEditor.length
        invalidate()
    }
    
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT or 
                             InputType.TYPE_TEXT_FLAG_MULTI_LINE or 
                             InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                             
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
    return object : BaseInputConnection(this, true) {
        override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
            text?.toString()?.let {
                if (AdaSeleksi()) {
                    GantiSeleksi(tekanLama = false, teksBaru = it)
                } else {
                    val awal = isiEditor.substring(0, posisiKursor)
                    val akhir = isiEditor.substring(posisiKursor)
                    isiEditor = awal + it + akhir
                    posisiKursor += it.length
                }
                constrainScroll()
                invalidate()
                ensureCursorVisible()
            }
            return true
        }

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            if (AdaSeleksi() && beforeLength > 0) {
                GantiSeleksi(tekanLama = false, teksBaru = "")
                constrainScroll()
                invalidate()
                ensureCursorVisible()
                return true
            }
            
            if (beforeLength > 0 && posisiKursor > 0) {
                val awal = isiEditor.substring(0, posisiKursor - beforeLength)
                val akhir = isiEditor.substring(posisiKursor)
                isiEditor = awal + akhir
                posisiKursor -= beforeLength
                constrainScroll()
                invalidate()
                ensureCursorVisible()
            }
            return true
        }

        override fun sendKeyEvent(event: KeyEvent?): Boolean {
            if (event?.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
                        commitText("\n", 1)
                        return true
                    }
                    KeyEvent.KEYCODE_DEL -> {
                        deleteSurroundingText(1, 0)
                        return true
                    }
                }
            }
            return super.sendKeyEvent(event)
            }
        }
    }


    override fun onCheckIsTextEditor(): Boolean = true
    
    private fun PilihKata(posisiInput: Int) {
        if (isiEditor.isEmpty()) {
            MulaiSeleksi(posisiInput)
            return
        }
        
        var posisi = posisiInput
        if (posisi >= isiEditor.length) {
            posisi = isiEditor.length - 1
        }

        var awal = posisi
        while (awal > 0 && isiEditor[awal - 1].isLetterOrDigit()) {
            awal--
        }

        var akhir = posisi
        while (akhir < isiEditor.length && isiEditor[akhir].isLetterOrDigit()) {
            akhir++
        }

        if (awal != akhir) {
            awalSeleksi = awal
            akhirSeleksi = akhir
            posisiKursor = akhir
            invalidate()
        } else {
            MulaiSeleksi(posisiInput) 
        }
    }
    
    private fun PilihKataAtauKarakterTerakhir(
        posisiInput: Int, sentuhX: Float, sentuhY: Float
    ) {
        if (isiEditor.isEmpty()) {
            BersihkanSeleksi()
            posisiKursor = 0
            return
        }

        val barisTeks = isiEditor.split("\n")
        val posisi = posisiInput.coerceIn(0, isiEditor.length)
        val (baris, kolom) = dapatkanPosisi(posisi)
        val teksBaris = barisTeks[baris]
        val awalBaris = posisi - kolom
        val akhirBaris = awalBaris + teksBaris.length
        val xAwalTeks = 50f
        val xAkhirTeks = xAwalTeks + warna.measureText(teksBaris)
        val hanyaSpasi = teksBaris.isNotEmpty() && teksBaris.all { it.isWhitespace() }
        
        if (!hanyaSpasi && teksBaris.isNotEmpty() && sentuhX > xAkhirTeks) {
            awalSeleksi = akhirBaris - 1
            akhirSeleksi = akhirBaris
            posisiKursor = akhirBaris
            invalidate()
            return
        }
        
        if (hanyaSpasi) {
            awalSeleksi = akhirBaris - 1
            akhirSeleksi = akhirBaris
            posisiKursor = akhirBaris
            invalidate()
            return
        }

        PilihKata(posisi)
    }
    
    fun getText(): String {
        return isiEditor
    }

    fun setColorString(terima: Int?) {
        warnaString = terima ?: Color.GREEN
        invalidate()
    }

    fun setColorCommand(awal: String?, akir: String?, terima: Int?) {
        warnaCommand = terima ?: Color.MAGENTA
        polaCommandAwal = awal
    
        if (!awal.isNullOrEmpty()) {
            polaCommandAkir = akir
        } else {
            polaCommandAkir = null
        }
        invalidate()
    }

    fun setColorNumber(terima: Int?) {
        warnaNumber = terima ?: Color.CYAN
        invalidate()
    }

    fun setColorOperator(kode: List<String>?, warna: Int?) {
        polaOperator = kode.orEmpty()
        warnaOperator = warna ?: globalText
        invalidate()
    }
    
    fun setColorType(kode: List<String>?, warna: Int?) {
        polaType = kode.orEmpty()
        warnaType = warna ?: globalText
        invalidate()
    }

    fun setColorVariable(kode: List<String>?, warna: Int?) {
        polaVariable = kode.orEmpty()
        warnaVariable = warna ?: globalText
        invalidate()
    }

    fun setColorBoolean(terima: Int?) {
        warnaBoolean = terima ?: Color.YELLOW
        invalidate()
    }

    fun setColorSelection(terima: Int?) {
        warnaSelection = terima ?: warnaLatarSeleksi
        warnaSeleksi.color = warnaSelection
        invalidate()
    }    
    
    fun setLanguage(terima: Boolean?) {
        kodeAktif = terima ?: false
        invalidate()
    }

    private fun calculateMaxScrollX(): Int {
        val barisTeks = isiEditor.split("\n")
        var maxWidth = 0f
        for (line in barisTeks) {
            val w = warna.measureText(line)
            if (w > maxWidth) maxWidth = w
        }
        val contentWidth = 50f + maxWidth + 50f 
        return maxOf(0, (contentWidth - width + 40f).toInt())
    }

    
    private fun calculateMaxScrollY(): Int {
        val jumlahBaris = isiEditor.count { it == '\n' } + 1
        val contentHeight = 50f + ((jumlahBaris - 1) * tinggiBaris) + 80f
        val tinggiBebas = getTinggiTerlihat()

        return maxOf(0, (contentHeight - tinggiBebas).toInt())
    }


    private fun constrainScroll() {
        val maxX = calculateMaxScrollX()
        val maxY = calculateMaxScrollY()
        var newX = scrollX
        var newY = scrollY
        
        if (newX < 0) newX = 0
        if (newX > maxX) newX = maxX
        if (newY < 0) newY = 0
        if (newY > maxY) newY = maxY
        
        if (newX != scrollX || newY != scrollY) {
            scrollTo(newX, newY)
        }
    }

    
    private fun ensureCursorVisible() {
        val pos = posisiKursor.coerceIn(0, isiEditor.length)
        val (baris, kolom) = dapatkanPosisi(pos)
        val barisTeks = isiEditor.split("\n")

        val x = 50f + warna.measureText( barisTeks[baris].substring(0, kolom) )
        val y = 50f + (baris * tinggiBaris)

        val tinggiBebas = getTinggiTerlihat()

    var targetX = scrollX
    var targetY = scrollY

    if (x < scrollX + 20f) {
        targetX = (x - 20f).toInt()
    } else if (x > scrollX + width - 30f) {
        targetX = (x - width + 30f).toInt()
    }
    if (y < scrollY + 20f) {
        targetY = (y - 20f).toInt()
    } else if (y > scrollY + tinggiBebas - 60f) {
        targetY = (y - tinggiBebas + 60f).toInt()
    }

        targetX = targetX.coerceIn(0, calculateMaxScrollX())
        targetY = targetY.coerceIn(0, calculateMaxScrollY())

        if (targetX != scrollX || targetY != scrollY) {
            scroller.forceFinished(true)
            scrollTo(targetX, targetY)
            invalidate()
        }
    }
    
    fun setFont(terima: Int?) {
        globalFont = if (terima != null) {
            ResourcesCompat.getFont(context, terima) ?: Typeface.DEFAULT
        } else {
            ResourcesCompat.getFont(
                context,R.font.arial_geo_bolditalic_webfont
            ) ?: Typeface.DEFAULT
        }

        warna.typeface = globalFont
        invalidate()
    }
    
}