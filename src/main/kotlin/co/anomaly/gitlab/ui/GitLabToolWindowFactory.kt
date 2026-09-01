package co.anomaly.gitlab.ui

import co.anomaly.gitlab.api.GitLabApiClient
import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.services.GitLabIssueService
import co.anomaly.gitlab.services.GitLabMilestoneService
import co.anomaly.gitlab.services.GitLabProjectService
import co.anomaly.gitlab.settings.GitLabSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class GitLabToolWindowFactory : com.intellij.openapi.wm.ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: com.intellij.openapi.wm.ToolWindow) {
        val settings = GitLabSettings.getInstance()

        val client = GitLabApiClient(settings.serverUrl, settings.privateToken)
        val projectService = GitLabProjectService(client)
        val milestoneService = GitLabMilestoneService(client)
        val issueService = GitLabIssueService(client)

        var allProjects: List<GitLabProject> = emptyList()
        try {
            allProjects = projectService.getProjects(1, 100)
        } catch (e: Exception) {
            Messages.showErrorDialog(toolWindow.component, "Failed to load projects: ${e.message}", "Error")
            return
        }

        var milestones: List<GitLabMilestone> = emptyList()
        var issues: List<co.anomaly.gitlab.models.GitLabIssue> = emptyList()
        var currentProjectId: Int = 0

        val projectSearchField = JBTextField().apply {
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
        }
        projectSearchField.foreground = javax.swing.UIManager.getColor("TextField.inactiveForeground")
        val projectTableModel = DefaultTableModel(arrayOf("Name", "Path"), 0)
        val projectTable = JBTable(projectTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        val milestoneTableModel = DefaultTableModel(arrayOf("Title", "State"), 0)
        val issueTableModel = DefaultTableModel(arrayOf("#", "Title", "State"), 0)

        val detailPanel = GitLabDetailPanel(issueService, milestoneService)

        fun handleProjectSelected(p: GitLabProject) {
            currentProjectId = p.id
            milestoneTableModel.setRowCount(0)
            try {
                if (settings.showOpenMilestonesOnly) {
                    milestones = milestoneService.getMilestones(p.id, state = "active")
                } else {
                    milestones = milestoneService.getMilestones(p.id)
                }
                milestones.forEach { m ->
                    milestoneTableModel.addRow(arrayOf(m.title, m.state))
                }
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to load milestones: ${e.message}", "Error")
            }

            issueTableModel.setRowCount(0)
            try {
                issues = issueService.getIssues(p.id)
                issues.forEach { issue ->
                    issueTableModel.addRow(arrayOf(issue.iid.toString(), issue.title, issue.state))
                }
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to load issues: ${e.message}", "Error")
            }

            detailPanel.removeAll()
            detailPanel.add(JBLabel("Select an item to view details").apply {
                font = UIUtil.getLabelFont().deriveFont(java.awt.Font.BOLD)
            }, BorderLayout.CENTER)
            detailPanel.revalidate()
            detailPanel.repaint()
        }

        val mainSplitter = com.intellij.ui.OnePixelSplitter(true, 0.4f)
        val detailSplitter = com.intellij.ui.OnePixelSplitter(true, 0.6f)

        projectSearchField.addActionListener {
            val currentText = projectSearchField.text
            val query = if (currentText == "Search projects...") "" else currentText.lowercase()
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

        val projectListPanel = JPanel(BorderLayout())
        projectListPanel.add(projectSearchField, BorderLayout.NORTH)
        projectListPanel.add(JScrollPane(projectTable), BorderLayout.CENTER)

        projectTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = projectTable.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = projectTable.convertRowIndexToModel(row)
                        val project = allProjects.getOrNull(modelRow)
                        project?.let { handleProjectSelected(it) }
                    }
                }
            }
        })

        projectTable.selectionModel.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val row = projectTable.selectedRow
                if (row >= 0) {
                    val modelRow = projectTable.convertRowIndexToModel(row)
                    val project = allProjects.getOrNull(modelRow)
                    project?.let { handleProjectSelected(it) }
                }
            }
        }

        val toolbarPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val refreshButton = JButton("Refresh")
        val createIssueButton = JButton("Create Issue")
        val createMilestoneButton = JButton("Create Milestone")
        val openOnlyCheckbox = JCheckBox("Open only", settings.showOpenMilestonesOnly)

        toolbarPanel.add(refreshButton)
        toolbarPanel.add(createIssueButton)
        toolbarPanel.add(createMilestoneButton)
        toolbarPanel.add(openOnlyCheckbox)
        projectListPanel.add(toolbarPanel, BorderLayout.SOUTH)

        val rightSplitter = com.intellij.ui.OnePixelSplitter(true, 0.5f)

        val milestonePanel = JPanel(BorderLayout())
        milestonePanel.border = BorderFactory.createTitledBorder("Milestones")
        val milestoneTableWrapper = JBTable(milestoneTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        val issuePanel = JPanel(BorderLayout())
        issuePanel.border = BorderFactory.createTitledBorder("Issues")
        val issueTableWrapper = JBTable(issueTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        rightSplitter.firstComponent = JScrollPane(milestonePanel)
        rightSplitter.secondComponent = JScrollPane(issuePanel)
        milestonePanel.add(JScrollPane(milestoneTableWrapper), BorderLayout.CENTER)
        issuePanel.add(JScrollPane(issueTableWrapper), BorderLayout.CENTER)

        detailSplitter.firstComponent = rightSplitter
        detailSplitter.secondComponent = JScrollPane(detailPanel)

        mainSplitter.firstComponent = JScrollPane(projectListPanel)
        mainSplitter.secondComponent = detailSplitter

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(mainSplitter, "", false)
        toolWindow.contentManager.addContent(content)

        allProjects.forEach { p ->
            projectTableModel.addRow(arrayOf(p.name, p.path))
        }

        refreshButton.addActionListener {
            try {
                allProjects = projectService.getProjects(1, 100)
                projectTableModel.setRowCount(0)
                allProjects.forEach { p ->
                    projectTableModel.addRow(arrayOf(p.name, p.path))
                }
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to refresh: ${e.message}", "Error")
            }
        }

        createIssueButton.addActionListener {
            showCreateIssueDialog(allProjects, milestones, issueService)
        }

        createMilestoneButton.addActionListener {
            if (currentProjectId != 0) {
                showCreateMilestoneDialog(currentProjectId, milestoneService)
            } else {
                Messages.showErrorDialog("Please select a project first.", "Error")
            }
        }

        openOnlyCheckbox.addActionListener {
            settings.showOpenMilestonesOnly = openOnlyCheckbox.isSelected
        }

        milestoneTableWrapper.getSelectionModel().addListSelectionListener { e ->
            if (!e.valueIsAdjusting && currentProjectId != 0) {
                val row = milestoneTableWrapper.selectedRow
                if (row >= 0) {
                    val modelRow = milestoneTableWrapper.convertRowIndexToModel(row)
                    val milestone = milestones.getOrNull(modelRow)
                    milestone?.let {
                        detailPanel.setDetails(GitLabDetailPanel.DETAILS_TYPE_MILESTONE, currentProjectId, it.iid)
                    }
                }
            }
        }

        issueTableWrapper.getSelectionModel().addListSelectionListener { e ->
            if (!e.valueIsAdjusting && currentProjectId != 0) {
                val row = issueTableWrapper.selectedRow
                if (row >= 0) {
                    val modelRow = issueTableWrapper.convertRowIndexToModel(row)
                    val issue = issues.getOrNull(modelRow)
                    issue?.let {
                        detailPanel.setDetails(GitLabDetailPanel.DETAILS_TYPE_ISSUE, currentProjectId, it.iid)
                    }
                }
            }
        }

        milestoneTableWrapper.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && currentProjectId != 0) {
                    val row = milestoneTableWrapper.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = milestoneTableWrapper.convertRowIndexToModel(row)
                        val milestone = milestones.getOrNull(modelRow)
                        milestone?.let {
                            detailPanel.setDetails(GitLabDetailPanel.DETAILS_TYPE_MILESTONE, currentProjectId, it.iid)
                        }
                    }
                }
            }
        })

        issueTableWrapper.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && currentProjectId != 0) {
                    val row = issueTableWrapper.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = issueTableWrapper.convertRowIndexToModel(row)
                        val issue = issues.getOrNull(modelRow)
                        issue?.let {
                            detailPanel.setDetails(GitLabDetailPanel.DETAILS_TYPE_ISSUE, currentProjectId, it.iid)
                        }
                    }
                }
            }
        })
    }

    private fun showCreateIssueDialog(
        projects: List<GitLabProject>,
        milestones: List<GitLabMilestone>,
        issueService: GitLabIssueService
    ) {
        CreateIssueDialog(projects, milestones, issueService) { }.apply {
            isVisible = true
        }
    }

    private fun showCreateMilestoneDialog(projectId: Int, milestoneService: GitLabMilestoneService) {
        CreateMilestoneDialog(projectId, milestoneService) { }.apply {
            isVisible = true
        }
    }
}
