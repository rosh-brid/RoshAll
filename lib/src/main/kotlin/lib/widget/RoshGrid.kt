package lib.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class RoshGrid @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    var spanCount = 2
        set(value) {
            field = max(1, value)
            requestLayout() 
        }

    var spacing = dpToPx(12)
        set(value) {
            field = value
            requestLayout()
        }

    private val loadingBar = LoadingBar(context).apply {
        layoutParams = LayoutParams(dpToPx(100), dpToPx(60))
        visibility = View.GONE 
    }

    init {
        super.addView(loadingBar)

        if (attrs != null) {
            val attributeArray = intArrayOf(android.R.attr.columnCount)
            val typedArray = context.obtainStyledAttributes(attrs, attributeArray)
            
            val xmlColumnCount = typedArray.getInt(0, -1)
            
            if (xmlColumnCount != -1) {
                spanCount = xmlColumnCount 
            }
            
            typedArray.recycle() 
        }
    }

    fun showLoading() {
        loadingBar.visibility = View.VISIBLE
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != loadingBar) child.visibility = View.GONE
        }
        requestLayout()
    }

    fun hideLoading() {
        loadingBar.visibility = View.GONE
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child != loadingBar) child.visibility = View.VISIBLE
        }
        requestLayout()
    }

    override fun removeAllViews() {
        super.removeAllViews()
        super.addView(loadingBar) 
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        
        if (loadingBar.visibility == View.VISIBLE) {
            measureChild(loadingBar, widthMeasureSpec, heightMeasureSpec)
            val minHeight = dpToPx(200)
            val finalHeight = max(minHeight, MeasureSpec.getSize(heightMeasureSpec))
            setMeasuredDimension(widthSize, finalHeight)
            return
        }

        val availableWidth = widthSize - paddingLeft - paddingRight
        val totalSpacing = spacing * (spanCount - 1)
        val itemWidth = (availableWidth - totalSpacing) / spanCount

        var totalHeight = paddingTop + paddingBottom
        var currentRowHeight = 0
        var visibleItemIndex = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child == loadingBar || child.visibility == View.GONE) continue

            val childWidthSpec = MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY)
            val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            
            child.measure(childWidthSpec, childHeightSpec)

            currentRowHeight = max(currentRowHeight, child.measuredHeight)

            if ((visibleItemIndex + 1) % spanCount == 0 || i == childCount - 1) {
                totalHeight += currentRowHeight
                if (i != childCount - 1) {
                    totalHeight += spacing 
                }
                currentRowHeight = 0 
            }
            visibleItemIndex++
        }

        setMeasuredDimension(widthSize, resolveSize(totalHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t

        if (loadingBar.visibility == View.VISIBLE) {
            val cx = width / 2
            val cy = height / 2
            val halfW = loadingBar.measuredWidth / 2
            val halfH = loadingBar.measuredHeight / 2
            loadingBar.layout(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
            return
        }

        val availableWidth = width - paddingLeft - paddingRight
        val totalSpacing = spacing * (spanCount - 1)
        val itemWidth = (availableWidth - totalSpacing) / spanCount

        var currentX = paddingLeft
        var currentY = paddingTop
        var currentRowHeight = 0
        var visibleItemIndex = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child == loadingBar || child.visibility == View.GONE) continue

            child.layout(currentX, currentY, currentX + itemWidth, currentY + child.measuredHeight)

            currentRowHeight = max(currentRowHeight, child.measuredHeight)

            if ((visibleItemIndex + 1) % spanCount == 0) {
                currentX = paddingLeft
                currentY += currentRowHeight + spacing
                currentRowHeight = 0
            } else {
                currentX += itemWidth + spacing
            }
            visibleItemIndex++
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}