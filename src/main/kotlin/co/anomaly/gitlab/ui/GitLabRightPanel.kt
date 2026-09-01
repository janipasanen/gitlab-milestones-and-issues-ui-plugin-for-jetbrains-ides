package co.anomaly.gitlab.ui

import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.services.GitLabIssueService
import co.anomaly.gitlab.services.GitLabMilestoneService
import co.anomaly.gitlab.settings.GitLabSettings
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.UIUtil
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class GitLabRightPanel(
    private val issueService: GitLabIssueService,
    private val milestoneService: GitLabMilestoneService
) : JPanel() {

    private var currentProject: GitLabProject? = null

    init {
        layout = BorderLayout()
    }

    fun setProject(project: GitLabProject?) {
        currentProject = project
        removeAll()

        if (project == null) {
            add(JBLabel("Select a project to view milestones and issues").apply {
                font = UIUtil.getLabelFont(UIUtil.FontSize.NORMAL).deriveFont(java.awt.Font.BOLD)
            }, BorderLayout.CENTER)
            revalidate()
            repaint()
            return
        }

        val mainPanel = JPanel(BorderLayout())
        val settings = GitLabSettings.getInstance()

        // Milestone list
        val milestonePanel = JPanel(BorderLayout())
        milestonePanel.border = BorderFactory.createTitledBorder("Milestones")
        val milestoneTableModel = DefaultTableModel(arrayOf("Title", "State"), 0)
        val milestoneTable = JBTable(milestoneTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        try {
            val milestones = if (settings.showOpenMilestonesOnly) {
                milestoneService.getMilestones(project.id, state = "active")
            } else {
                milestoneService.getMilestones(project.id)
            }
            milestones.forEach { m ->
                milestoneTableModel.addRow(arrayOf(m.title, m.state))
            }
        } catch (e: Exception) {
            Messages.showErrorDialog("Failed to load milestones: ${e.message}", "Error")
        }

        // Issue list
        val issuePanel = JPanel(BorderLayout())
        issuePanel.border = BorderFactory.createTitledBorder("Issues")
        val issueTableModel = DefaultTableModel(arrayOf("#", "Title", "State"), 0)
        val issueTable = JBTable(issueTableModel).apply {
            rowHeight = 24
            autoCreateRowSorter = true
        }

        try {
            val issues = issueService.getIssues(project.id)
            issues.forEach { issue ->
                issueTableModel.addRow(arrayOf(issue.iid.toString(), issue.title, issue.state))
            }
        } catch (e: Exception) {
            Messages.showErrorDialog("Failed to load issues: ${e.message}", "Error")
        }

        val rightSplitter = com.intellij.ui.OnePixelSplitter(true, 0.5f)
        rightSplitter.firstComponent = JScrollPane(milestonePanel)
        rightSplitter.secondComponent = JScrollPane(issuePanel)
        milestonePanel.add(JScrollPane(milestoneTable), BorderLayout.CENTER)
        issuePanel.add(JScrollPane(issueTable), BorderLayout.CENTER)

        mainPanel.add(rightSplitter, BorderLayout.CENTER)
        add(mainPanel, BorderLayout.CENTER)
    }
}
