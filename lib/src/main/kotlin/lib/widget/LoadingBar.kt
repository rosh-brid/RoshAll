package lib.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import lib.R

class LoadingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokeSize = 16f 

    private val bgPaint = Paint().apply {
        style = Paint.Style.STROKE 
        strokeWidth = strokeSize
        strokeCap = Paint.Cap.ROUND 
        isAntiAlias = true
        color = try {
            ContextCompat.getColor(context, R.color.parent)
        } catch (e: Exception) {
            Color.parseColor("#E0E0E0")
        }
    }

    private val barPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = try {
            ContextCompat.getColor(context, R.color.text)
        } catch (e: Exception) {
            Color.parseColor("#2196F3")
        }
    }

    private var animator: ValueAnimator? = null
    private var animationFraction = 0f

    private val infinityPath = Path()
    private var pathMeasure = PathMeasure()
    private var pathLength = 0f
    
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    init {
        setupAnimation()
    }

    private fun setupAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000 
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                animationFraction = animation.animatedValue as Float
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createInfinityPath(w.toFloat(), h.toFloat())
    }

    private fun createInfinityPath(w: Float, h: Float) {
        infinityPath.reset()
        
        val pad = strokeSize
        val cx = w / 2f
        val cy = h / 2f
        
        infinityPath.moveTo(cx, cy) 
        
        infinityPath.cubicTo(
            cx + w/4, pad,          
            w - pad, pad,           
            w - pad, cy             
        )
        infinityPath.cubicTo(
            w - pad, h - pad,       
            cx + w/4, h - pad,      
            cx, cy                  
        )
        
        infinityPath.cubicTo(
            cx - w/4, pad,          
            pad, pad,               
            pad, cy                 
        )
        infinityPath.cubicTo(
            pad, h - pad,           
            cx - w/4, h - pad,      
            cx, cy                  
        )

        pathMeasure = PathMeasure(infinityPath, false)
        pathLength = pathMeasure.length
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(infinityPath, bgPaint)
        drawSnake(canvas)
    }

    private fun drawSnake(canvas: Canvas) {
        if (pathLength == 0f) return 

        val snakeLength = pathLength * 0.20f 
        
        val startD = animationFraction * pathLength 
        val stopD = startD + snakeLength

        val step = 2f 
        
        var currentD = startD
        while (currentD <= stopD) {
            val actualD = currentD % pathLength

            pathMeasure.getPosTan(actualD, pos, tan)

            val fraction = (currentD - startD) / snakeLength
            
            val currentRadius = (strokeSize / 2f) * fraction

            canvas.drawCircle(pos[0], pos[1], currentRadius, barPaint)

            currentD += step
        }
    }

    fun setBackground(color: Int) {
        bgPaint.color = color
        invalidate()
    }

    fun setBar(color: Int) {
        barPaint.color = color
        invalidate()
    }
}