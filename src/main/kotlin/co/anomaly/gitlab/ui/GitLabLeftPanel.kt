package co.anomaly.gitlab.ui

import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.services.GitLabProjectService
import co.anomaly.gitlab.services.GitLabIssueService
import co.anomaly.gitlab.services.GitLabMilestoneService
import co.anomaly.gitlab.settings.GitLabSettings
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.*
import java.util.concurrent.CopyOnWriteArrayList

class GitLabLeftPanel(
    private val projectService: GitLabProjectService,
    private val issueService: GitLabIssueService,
    private val milestoneService: GitLabMilestoneService
) : JPanel() {

    private var allProjects: MutableList<GitLabProject> = CopyOnWriteArrayList()
    private var currentProjectId: Int = 0
    private var currentMilestones: List<GitLabMilestone> = emptyList()
    private var currentIssues: List<GitLabIssue> = emptyList()

    private var projectSearchField: JBTextField? = null
    private var milestoneSearchField: JBTextField? = null

    init {
        layout = BorderLayout()
        loadProjectsAndSetupUI()
    }

    private fun loadProjectsAndSetupUI() {
        val settings = GitLabSettings.getInstance()

        val searchPanel = JPanel(BorderLayout())
        projectSearchField = JBTextField().apply {
            text = "Search projects..."
            addFocusListener(object : java.awt.event.FocusAdapter() {
                override fun focusGained(e: java.awt.event.FocusEvent?) {
                    if (text == "Search projects...") {
                        text = ""
                        foreground = javax.swing.UIManager.getColor("TextField.foreground")
                    }
                }
                override fun focusLost(e: java.awt.event.FocusEvent?) {
                    if (text.isEmpty()) {
                        text = "Search projects..."
                        foreground = javax.swing.UIManager.getColor("TextField.inactiveForeground")
                    }
                }
            })
            foreground = javax.swing.UIManager.getColor("TextField.inactiveForeground")
        }
        searchPanel.add(projectSearchField, BorderLayout.CENTER)

        val projectTableModel = DefaultTableModel(arrayOf("Name", "Path"), 0)
        val projectTable = JBTable(projectTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        val toolbarPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val refreshButton = JButton("Refresh")
        toolbarPanel.add(refreshButton)

        refreshButton.addActionListener {
            try {
                allProjects.clear()
                allProjects.addAll(projectService.getProjects(1, 100))
                projectTableModel.setRowCount(0)
                allProjects.forEach { p ->
                    projectTableModel.addRow(arrayOf(p.name, p.path))
                }
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to refresh: ${e.message}", "Error")
            }
        }

        projectSearchField!!.addActionListener {
            val searchText = projectSearchField!!.text ?: ""
            val query = if (searchText == "Search projects...") "" else searchText.lowercase()
            projectTableModel.setRowCount(0)
            if (query.isBlank()) {
                allProjects.forEach { p ->
                    projectTableModel.addRow(arrayOf(p.name, p.path))
                }
            } else {
                allProjects.filter {
                    it.name.lowercase().contains(query) || it.path.lowercase().contains(query)
                }.forEach { p ->
                    projectTableModel.addRow(arrayOf(p.name, p.path))
                }
            }
        }

        projectTable.selectionModel.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val row = projectTable.selectedRow
                if (row >= 0) {
                    val modelRow = projectTable.convertRowIndexToModel(row)
                    val project = allProjects.getOrNull(modelRow)
                    project?.let { loadProjectData(it) }
                }
            }
        }

        projectTable.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    val row = projectTable.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = projectTable.convertRowIndexToModel(row)
                        val project = allProjects.getOrNull(modelRow)
                        project?.let { loadProjectData(it) }
                    }
                }
            }
        })

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(searchPanel, BorderLayout.NORTH)
        mainPanel.add(JScrollPane(projectTable), BorderLayout.CENTER)
        mainPanel.add(toolbarPanel, BorderLayout.SOUTH)

        add(mainPanel, BorderLayout.CENTER)

        allProjects.addAll(projectService.getProjects(1, 100))
        allProjects.forEach { p ->
            projectTableModel.addRow(arrayOf(p.name, p.path))
        }
    }

    private fun loadProjectData(project: GitLabProject) {
        currentProjectId = project.id
        val settings = GitLabSettings.getInstance()

        ProgressManager.getInstance().run(object : Task.Backgroundable(null, "Loading project data...", true) {
            override fun run(progress: com.intellij.openapi.progress.ProgressIndicator) {
                progress.isIndeterminate = true
                try {
                    currentMilestones = if (settings.showOpenMilestonesOnly) {
                        milestoneService.getMilestones(project.id, state = "active")
                    } else {
                        milestoneService.getMilestones(project.id)
                    }
                    currentIssues = issueService.getIssues(project.id)
                } catch (e: Exception) {
                    // Error handling in UI thread
                    com.intellij.openapi.application.invokeLater {
                        Messages.showErrorDialog("Failed to load project data: ${e.message}", "Error")
                    }
                }
                progress.fraction = 1.0
            }
        })
    }
}
