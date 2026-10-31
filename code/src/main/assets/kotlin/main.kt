
import android.graphics.*

class KotlingLang {

    fun getBoolean(): List<String> {
        return listOf(
            "true",
            "false"
        )
    }

    fun getKeyword(): List<String> {
        return listOf(
            "val","var","fun",
            "class","interface", "object",
            "private", "public", "internal",
            "protected", "lateinit", "const",
            "override", "return","import",
            "package"
        )
    }
    
    fun getType():List<String>{
        return listOf(
            "Int","String","Float",
            "Double","Boolean","Long",
            "Unit","Char"
        )
    }
    
    fun getColorKeyword():Int{
        return Color.parseColor("#aa3333")
    }
    
    fun getColorBoolean():Int{
        return Color.parseColor("#aa3333")
    }
    
    fun getColorType():Int{
        return Color.parseColor("#33aaaa")
    }
}