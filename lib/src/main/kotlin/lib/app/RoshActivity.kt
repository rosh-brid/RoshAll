package lib.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*

open class RoshActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.enableEdgeToEdge(window)

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        val gelap = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        controller.isAppearanceLightStatusBars = !gelap
        controller.isAppearanceLightNavigationBars = !gelap
        
        window.statusBarColor = if(gelap){Color.BLACK} else {Color.WHITE}
        window.navigationBarColor = if(gelap){Color.BLACK} else {Color.WHITE}
    }

    fun setView(layout: Int) {
        setContentView(layout)

        val root = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }
}