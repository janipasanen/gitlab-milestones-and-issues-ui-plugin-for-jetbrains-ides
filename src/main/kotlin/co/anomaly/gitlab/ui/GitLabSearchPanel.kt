package co.anomaly.gitlab.ui

import co.anomaly.gitlab.search.SearchResultItem
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class GitLabSearchPanel(
    private val searchService: co.anomaly.gitlab.search.SearchService
) : JPanel() {

    private var searchResults: List<SearchResultItem> = emptyList()

    init {
        layout = BorderLayout()
    }

    fun performSearch(query: String) {
        if (query.isBlank()) {
            removeAll()
        add(javax.swing.JLabel("Enter a search term...").apply {
            font = UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(java.awt.Font.BOLD)
        }, BorderLayout.CENTER)
            revalidate()
            repaint()
            return
        }

        val panel = JPanel(BorderLayout())
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true
        panel.add(progressBar, BorderLayout.CENTER)
        removeAll()
        add(panel)

        revalidate()
        repaint()

        SwingUtilities.invokeLater {
            ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Searching...", true) {
                override fun run(progress: com.intellij.openapi.progress.ProgressIndicator) {
                    progress.isIndeterminate = true
                    try {
                        searchResults = searchService.searchAll(query)
                        populateResults()
                    } catch (e: Exception) {
                        Messages.showErrorDialog("Search failed: ${e.message}", "Error")
                    }
                    progress.fraction = 1.0
                }
            })
        }
    }

    private fun populateResults() {
        val model = DefaultTableModel(arrayOf("Type", "Name"), 0)
        searchResults.forEach { item ->
            model.addRow(arrayOf(item.type, item.displayName))
        }

        val table = JBTable(model).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        removeAll()
        add(JScrollPane(table), BorderLayout.CENTER)
        revalidate()
        repaint()

        table.getSelectionModel().addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val row = table.selectedRow
                if (row >= 0) {
                    val modelRow = table.convertRowIndexToModel(row)
                    val item = searchResults.getOrNull(modelRow)
                    item?.let { onSearchResultSelected?.invoke(item) }
                }
            }
        }

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = table.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = table.convertRowIndexToModel(row)
                        val item = searchResults.getOrNull(modelRow)
                        item?.let { onSearchResultSelected?.invoke(item) }
                    }
                }
            }
        })
    }

    fun setSearchResults(results: List<SearchResultItem>) {
        searchResults = results
        populateResults()
    }

    var onSearchResultSelected: ((SearchResultItem) -> Unit)? = null
}
