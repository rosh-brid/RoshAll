package lib.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.*
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.*

data class FileNode(
    val doc: DocumentFile,
    val depth: Int,
    var isExpanded: Boolean = false
)

class FolderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var targetDirectory: DocumentFile
    private var selectedDoc: DocumentFile? = null
    private val expandedPaths = mutableSetOf<String>()
    private var onFileSelected: ((DocumentFile) -> Unit)? = null
    private var onFileLongClick: ((DocumentFile) -> Unit)? = null

    private val hScrollView: HorizontalScrollView
    private val recyclerView: RecyclerView
    private val emptyView: TextView
    private val progressBar: LoadingBar
    private val treeAdapter: TreeAdapter
    
    // Coroutine scope untuk background loading agar tidak lemot
    private var viewScope = CoroutineScope(Dispatchers.Main + Job())

    init {
        val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        targetDirectory = DocumentFile.fromFile(fallbackDir)

        hScrollView = HorizontalScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            isFillViewport = true
        }

        recyclerView = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
        
        hScrollView.addView(recyclerView)

        emptyView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, lib.R.color.text))
            text = "Empty Directory"
            visibility = GONE
        }

val loadingSizePx = dpToPx(context, 70f)

progressBar = LoadingBar(context).apply {
    layoutParams = LayoutParams(loadingSizePx, loadingSizePx).apply {
        gravity = Gravity.CENTER
    }
    visibility = GONE
}


        treeAdapter = TreeAdapter(
            onItemClick = { node, pos -> onNodeClicked(node, pos) },
            onItemLongClick = { node, pos -> onNodeLongClicked(node, pos) }
        )
        recyclerView.adapter = treeAdapter
        
        addView(hScrollView)
        addView(emptyView)
        addView(progressBar) // Tambahkan terakhir agar posisinya di atas (Z-index paling atas)

        refresh()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewScope = CoroutineScope(Dispatchers.Main + Job())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewScope.cancel() // Cegah memory leak saat view dihancurkan
    }

    private fun onNodeClicked(node: FileNode, position: Int) {
        if (node.doc.isDirectory) {
            treeAdapter.toggleFolder(node, position)
            if (node.isExpanded) expandedPaths.add(node.doc.uri.toString())
            else expandedPaths.remove(node.doc.uri.toString())
        } else {
            selectedDoc = node.doc
            treeAdapter.setSelectedPosition(position)
            onFileSelected?.invoke(node.doc)
        }
    }

    private fun onNodeLongClicked(node: FileNode, position: Int): Boolean {
        if (position == RecyclerView.NO_POSITION) return false
        val listener = onFileLongClick ?: return false
        listener.invoke(node.doc)
        return true
    }

    fun refresh() {
        progressBar.visibility = VISIBLE
        viewScope.launch {
            val visible = mutableListOf<FileNode>()
            // Pindah ke background thread agar UI tidak lemot
            withContext(Dispatchers.IO) {
                try {
                    val files = targetDirectory.listFiles().toList()
                    val sorted = files.sortedWith(
                        compareBy({ !it.isDirectory }, { (it.name ?: "").lowercase() })
                    )
                    for (f in sorted) {
                        val node = FileNode(f, 0, isExpanded = expandedPaths.contains(f.uri.toString()))
                        visible.add(node)
                        if (node.isExpanded && f.isDirectory) restoreChildren(visible, node)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            
            treeAdapter.setRootItems(visible)
            updateEmptyState()
            progressBar.visibility = GONE
        }
    }

    fun setUriDir(terima: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(terima, flags)
        } catch (e: SecurityException) {
            Log.e("FolderView", "Gagal mengambil hak akses URI", e)
        }

        val doc = DocumentFile.fromTreeUri(context, terima)
            ?: DocumentFile.fromSingleUri(context, terima)

        if (doc != null && doc.exists() && doc.isDirectory) {
            applyDirectory(doc)
        } else {
            Log.w("FolderView", "Need Directory: $terima")
        }
    }

    private fun applyDirectory(doc: DocumentFile) {
        targetDirectory = doc
        expandedPaths.clear()
        selectedDoc = null
        treeAdapter.clearSelection()
        refresh()
    }

    // ---- API publik ----

    fun getCurrentDir(): DocumentFile = targetDirectory
    fun getCurrentUri(): Uri = targetDirectory.uri

    fun getSelectedDoc(): DocumentFile? = selectedDoc
    fun getSelectedUri(): Uri? = selectedDoc?.uri

    fun clearSelection() {
        selectedDoc = null
        treeAdapter.clearSelection()
    }

    fun setOnFileSelectedListener(listener: (DocumentFile) -> Unit) {
        onFileSelected = listener
    }

    fun setOnFileLongClickListener(listener: (DocumentFile) -> Unit) {
        onFileLongClick = listener
    }

    fun scrollToPath(path: String) {
        val idx = treeAdapter.indexOfPath(path)
        if (idx >= 0) recyclerView.scrollToPosition(idx)
    }

    // Dipanggil di dalam Dispatchers.IO
    private fun restoreChildren(visible: MutableList<FileNode>, parent: FileNode) {
        val children = try {
            parent.doc.listFiles().toList()
        } catch (e: SecurityException) {
            emptyList()
        }
        val sorted = children.sortedWith(
            compareBy({ !it.isDirectory }, { (it.name ?: "").lowercase() })
        )
        for (f in sorted) {
            val node = FileNode(
                f,
                parent.depth + 1,
                isExpanded = expandedPaths.contains(f.uri.toString())
            )
            visible.add(node)
            if (node.isExpanded && f.isDirectory) restoreChildren(visible, node)
        }
    }

    private fun updateEmptyState() {
        val empty = treeAdapter.itemCount == 0
        emptyView.visibility = if (empty) VISIBLE else GONE
        hScrollView.visibility = if (empty) GONE else VISIBLE
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    private inner class TreeAdapter(
        private val onItemClick: (FileNode, Int) -> Unit,
        private val onItemLongClick: (FileNode, Int) -> Boolean
    ) : RecyclerView.Adapter<TreeAdapter.TreeViewHolder>() {

        private val visibleNodes: MutableList<FileNode> = mutableListOf()
        private var selectedPosition: Int = RecyclerView.NO_POSITION

        fun setRootItems(newNodes: List<FileNode>) {
            visibleNodes.clear()
            visibleNodes.addAll(newNodes)
            selectedPosition = RecyclerView.NO_POSITION
            notifyDataSetChanged()
        }

        fun setSelectedPosition(position: Int) {
            val old = selectedPosition
            selectedPosition = position
            if (old != RecyclerView.NO_POSITION && old < visibleNodes.size) {
                notifyItemChanged(old)
            }
            if (position != RecyclerView.NO_POSITION && position < visibleNodes.size) {
                notifyItemChanged(position)
            }
        }

        fun clearSelection() {
            setSelectedPosition(RecyclerView.NO_POSITION)
        }

        fun indexOfPath(path: String): Int =
            visibleNodes.indexOfFirst { it.doc.uri.toString() == path }

        fun toggleFolder(node: FileNode, position: Int) {
            if (position == RecyclerView.NO_POSITION || position >= visibleNodes.size) return

            if (node.isExpanded) {
                var removeCount = 0

                while (position + 1 < visibleNodes.size && visibleNodes[position + 1].depth > node.depth) {
                    visibleNodes.removeAt(position + 1)
                    removeCount++
                }

                node.isExpanded = false
                if (removeCount > 0) notifyItemRangeRemoved(position + 1, removeCount)
                notifyItemChanged(position)
                updateEmptyState()
            } else {
                // Tampilkan loading saat membuka folder
                progressBar.visibility = VISIBLE
                
                viewScope.launch {
                    val childNodes = mutableListOf<FileNode>()
                    
                    // Pindah ke background thread
                    withContext(Dispatchers.IO) {
                        val children = try {
                            node.doc.listFiles().toList()
                        } catch (e: SecurityException) {
                            emptyList()
                        }

                        val sortedChildren = children.sortedWith(
                            compareBy({ !it.isDirectory }, { (it.name ?: "").lowercase() })
                        )

                        for (file in sortedChildren) {
                            val child = FileNode(
                                file,
                                node.depth + 1,
                                isExpanded = expandedPaths.contains(file.uri.toString())
                            )
                            childNodes.add(child)
                            if (child.isExpanded && file.isDirectory) {
                                restoreChildren(childNodes, child)
                            }
                        }
                    }

                    // Kembali ke main thread untuk update UI
                    visibleNodes.addAll(position + 1, childNodes)
                    node.isExpanded = true

                    if (childNodes.isNotEmpty()) notifyItemRangeInserted(position + 1, childNodes.size)
                    notifyItemChanged(position)
                    updateEmptyState()
                    
                    // Sembunyikan loading
                    progressBar.visibility = GONE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
            val itemContext = parent.context

            val container = LinearLayout(itemContext).apply {
                // 3. Ubah width menjadi WRAP_CONTENT agar bisa menyamping
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Set min width agar background selection tetap memenuhi layar minimal selebar layar
                minimumWidth = resources.displayMetrics.widthPixels
                
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                isClickable = true
                isFocusable = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val tv = TypedValue()
                    if (itemContext.theme.resolveAttribute(
                            android.R.attr.selectableItemBackground, tv, true
                        ) && tv.resourceId != 0
                    ) {
                        foreground = ContextCompat.getDrawable(itemContext, tv.resourceId)
                    }
                }
            }

            val indicatorView = TextView(itemContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(itemContext, 20f),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(itemContext, lib.R.color.text))
            }

            val iconView = ImageView(itemContext).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(itemContext, 18f),
                    dpToPx(itemContext, 18f)
                ).apply { marginEnd = dpToPx(itemContext, 8f) }
            }

            val textView = TextView(itemContext).apply {
                // Lebar dibuat WRAP_CONTENT, hapus weight agar bisa memanjang terus
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 14f
                setTextColor(ContextCompat.getColor(itemContext, lib.R.color.text))
                isSingleLine = true
                // HAPUS ellipsize agar text tidak terpotong (...) tapi justru mendesak scrollview
                // ellipsize = TextUtils.TruncateAt.MIDDLE 
            }

            container.addView(indicatorView)
            container.addView(iconView)
            container.addView(textView)

            return TreeViewHolder(container, indicatorView, iconView, textView)
        }

        override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
            val node = visibleNodes[position]
            holder.bind(node, position == selectedPosition)
            
            holder.itemView.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onItemClick(node, currentPos)
                }
            }
            
            holder.itemView.setOnLongClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onItemLongClick(node, currentPos)
                } else {
                    false
                }
            }
        }

        override fun getItemCount(): Int = visibleNodes.size

        inner class TreeViewHolder(
            view: View,
            private val indicator: TextView,
            private val icon: ImageView,
            private val title: TextView
        ) : RecyclerView.ViewHolder(view) {

            private val hostActivity: Activity? = resolveActivity(view.context)
            private val ikonFile: lib.view.IconFile? = hostActivity?.let { lib.view.IconFile(it) }

            fun bind(node: FileNode, isSelected: Boolean) {
                title.text = node.doc.name ?: ""

                val basePadding = dpToPx(itemView.context, 8f)
                val indent = dpToPx(itemView.context, (node.depth * 20).toFloat())
                
                // Tambahkan extra padding kanan agar saat scroll sampai mentok tidak nempel banget
                itemView.setPadding(indent, basePadding, basePadding + dpToPx(itemView.context, 16f), basePadding)

                Glide.with(icon).clear(icon)

                if (node.doc.isDirectory) {
                    icon.setImageResource(lib.R.drawable.folder)
                    indicator.text = if (node.isExpanded) "▼" else "▶"
                    title.setTypeface(null, Typeface.BOLD)
                } else {
                    if (ikonFile != null) {
                        ikonFile.setBipmap(icon)
                        icon.setImageResource(ikonFile.setUri(node.doc.uri)) 
                    } else {
                        Glide.with(icon).load(node.doc.uri).into(icon)
                    }
                    indicator.text = ""
                    title.setTypeface(null, Typeface.NORMAL)
                }

                itemView.setBackgroundColor(
                    if (isSelected) ContextCompat.getColor(itemView.context, lib.R.color.parent)
                    else Color.TRANSPARENT
                )
            }
        }
    }

    companion object {
        private fun resolveActivity(ctx: Context): Activity? {
            var c: Context? = ctx
            while (c is ContextWrapper) {
                if (c is Activity) return c
                c = c.baseContext
            }
            return null
        }
    }
}
