package io.phinfotech.gitlab.ui

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.models.GitLabProject
import io.phinfotech.gitlab.models.GitLabMilestone
import io.phinfotech.gitlab.models.GitLabPipeline
import io.phinfotech.gitlab.services.GitLabIssueService
import io.phinfotech.gitlab.services.GitLabMilestoneService
import io.phinfotech.gitlab.services.GitLabPipelineService
import io.phinfotech.gitlab.services.GitLabProjectService
import io.phinfotech.gitlab.settings.GitLabSettings
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
        val pipelineService = GitLabPipelineService(client)

        var allProjects: List<GitLabProject> = emptyList()
        try {
            allProjects = projectService.getProjects(1, 100)
        } catch (e: Exception) {
            Messages.showErrorDialog(toolWindow.component, "Failed to load projects: ${e.message}", "Error")
            return
        }

        var milestones: List<GitLabMilestone> = emptyList()
        var issues: List<io.phinfotech.gitlab.models.GitLabIssue> = emptyList()
        var pipelines: List<GitLabPipeline> = emptyList()
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
        val pipelineTableModel = DefaultTableModel(arrayOf("ID", "Status", "Branch", "Commit", "Created"), 0)

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

            pipelineTableModel.setRowCount(0)
            try {
                pipelines = pipelineService.getPipelines(p.id)
                pipelines.forEach { pipeline ->
                    pipelineTableModel.addRow(arrayOf(
                        pipeline.iid.toString(),
                        pipeline.status,
                        pipeline.ref,
                        pipeline.commit?.message?.take(30) ?: "",
                        pipeline.created_at.orEmpty()
                    ))
                }
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to load pipelines: ${e.message}", "Error")
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

        // Main content area with tabbed pane
        val contentPanel = JPanel(BorderLayout())
        
        // Right panel with milestones/issues/pipelines tabbed
        val tabbedPane = JTabbedPane()
        
        // Milestones tab
        val milestonePanel = JPanel(BorderLayout())
        milestonePanel.border = BorderFactory.createTitledBorder("Milestones")
        val milestoneTableWrapper = JBTable(milestoneTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }
        milestonePanel.add(JScrollPane(milestoneTableWrapper), BorderLayout.CENTER)
        tabbedPane.addTab("Milestones", milestonePanel)
        
        // Issues tab
        val issuePanel = JPanel(BorderLayout())
        issuePanel.border = BorderFactory.createTitledBorder("Issues")
        val issueTableWrapper = JBTable(issueTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }
        issuePanel.add(JScrollPane(issueTableWrapper), BorderLayout.CENTER)
        tabbedPane.addTab("Issues", issuePanel)
        
        // Pipelines tab
        val pipelinePanel = JPanel(BorderLayout())
        pipelinePanel.border = BorderFactory.createTitledBorder("Pipelines")
        val pipelineTableWrapper = JBTable(pipelineTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }
        
        // Pipeline toolbar with stop/retry/cancel buttons
        val pipelineToolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val stopPipelineButton = JButton("Stop")
        val retryPipelineButton = JButton("Retry")
        val cancelPipelineButton = JButton("Cancel")
        
        stopPipelineButton.isEnabled = false
        retryPipelineButton.isEnabled = false
        cancelPipelineButton.isEnabled = false
        
        pipelineToolbarPanel.add(stopPipelineButton)
        pipelineToolbarPanel.add(retryPipelineButton)
        pipelineToolbarPanel.add(cancelPipelineButton)
        
        val pipelinePanelWrapper = JPanel(BorderLayout())
        pipelinePanelWrapper.add(pipelineToolbarPanel, BorderLayout.NORTH)
        pipelinePanelWrapper.add(JScrollPane(pipelinePanel), BorderLayout.CENTER)
        
        tabbedPane.addTab("Pipelines", pipelinePanelWrapper)
        
        val rightSplitter = com.intellij.ui.OnePixelSplitter(true, 0.5f)
        rightSplitter.firstComponent = JScrollPane(tabbedPane)
        rightSplitter.secondComponent = JScrollPane(detailPanel)

        detailSplitter.firstComponent = rightSplitter
        detailSplitter.secondComponent = detailPanel

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
        
        // Pipeline table selection listeners
        var selectedPipelineIid: Int? = null
        
        pipelineTableWrapper.getSelectionModel().addListSelectionListener { e ->
            if (!e.valueIsAdjusting && currentProjectId != 0) {
                val row = pipelineTableWrapper.selectedRow
                if (row >= 0) {
                    val modelRow = pipelineTableWrapper.convertRowIndexToModel(row)
                    val pipeline = pipelines.getOrNull(modelRow)
                    selectedPipelineIid = pipeline?.iid
                    
                    // Enable/disable pipeline action buttons based on status
                    val enabled = pipeline?.status == "running" || pipeline?.status == "pending" || pipeline?.status == "failed"
                    stopPipelineButton.isEnabled = enabled && pipeline?.status == "running" || pipeline?.status == "pending"
                    retryPipelineButton.isEnabled = pipeline?.status == "failed" || pipeline?.status == "canceled"
                    cancelPipelineButton.isEnabled = pipeline?.status == "running" || pipeline?.status == "pending"
                } else {
                    selectedPipelineIid = null
                    stopPipelineButton.isEnabled = false
                    retryPipelineButton.isEnabled = false
                    cancelPipelineButton.isEnabled = false
                }
            }
        }
        
        pipelineTableWrapper.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && currentProjectId != 0) {
                    val row = pipelineTableWrapper.rowAtPoint(e.point)
                    if (row >= 0) {
                        val modelRow = pipelineTableWrapper.convertRowIndexToModel(row)
                        val pipeline = pipelines.getOrNull(modelRow)
                        if (pipeline != null) {
                            Messages.showInfoMessage(
                                "Pipeline #${pipeline.iid}\nStatus: ${pipeline.status}\nBranch: ${pipeline.ref}\nCommit: ${pipeline.commit?.message?.take(100) ?: pipeline.sha}\nCreated: ${pipeline.created_at}",
                                "Pipeline Details"
                            )
                        }
                    }
                }
            }
        })
        
        // Pipeline action buttons
        stopPipelineButton.addActionListener {
            if (selectedPipelineIid != null) {
                try {
                    pipelineService.stopPipeline(currentProjectId, selectedPipelineIid!!)
                    handleProjectSelected(allProjects.find { it.id == currentProjectId } ?: return@addActionListener)
                    Messages.showInfoMessage("Pipeline stopped successfully.", "Success")
                } catch (e: Exception) {
                    Messages.showErrorDialog("Failed to stop pipeline: ${e.message}", "Error")
                }
            }
        }
        
        retryPipelineButton.addActionListener {
            if (selectedPipelineIid != null) {
                try {
                    pipelineService.retryPipeline(currentProjectId, selectedPipelineIid!!)
                    handleProjectSelected(allProjects.find { it.id == currentProjectId } ?: return@addActionListener)
                    Messages.showInfoMessage("Pipeline retried successfully.", "Success")
                } catch (e: Exception) {
                    Messages.showErrorDialog("Failed to retry pipeline: ${e.message}", "Error")
                }
            }
        }
        
        cancelPipelineButton.addActionListener {
            if (selectedPipelineIid != null) {
                try {
                    pipelineService.cancelPipeline(currentProjectId, selectedPipelineIid!!)
                    handleProjectSelected(allProjects.find { it.id == currentProjectId } ?: return@addActionListener)
                    Messages.showInfoMessage("Pipeline canceled successfully.", "Success")
                } catch (e: Exception) {
                    Messages.showErrorDialog("Failed to cancel pipeline: ${e.message}", "Error")
                }
            }
        }
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
