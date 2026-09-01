package co.anomaly.gitlab.ui

import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.search.SearchResultItem
import co.anomaly.gitlab.search.SearchService
import co.anomaly.gitlab.services.GitLabIssueService
import co.anomaly.gitlab.services.GitLabMilestoneService
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class GitLabDetailPanel(
    private val issueService: GitLabIssueService,
    private val milestoneService: GitLabMilestoneService
) : JPanel() {

    companion object {
        const val DETAILS_TYPE_MILESTONE = "milestone"
        const val DETAILS_TYPE_ISSUE = "issue"
    }

    private var currentType: String = ""
    private var currentProjectId: Int = 0
    private var currentIid: Int = 0
    private var currentData: Any? = null

    init {
        layout = BorderLayout()
        border = EmptyBorder(10, 10, 10, 10)
        init()
    }

    private fun init() {
        add(createEmptyStatePanel(), BorderLayout.CENTER)
    }

    fun setDetails(type: String, projectId: Int, iid: Int) {
        currentType = type
        currentProjectId = projectId
        currentIid = iid

        removeAll()

        val panel = JPanel(BorderLayout())
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true
        panel.add(progressBar, BorderLayout.CENTER)
        add(panel)

        revalidate()
        repaint()

        com.intellij.openapi.application.invokeLater {
            fetchDetails(type, projectId, iid, panel)
        }
    }

    private fun fetchDetails(type: String, projectId: Int, iid: Int, container: JComponent) {
        if (type == DETAILS_TYPE_ISSUE) {
            try {
                val issue = issueService.getIssue(projectId, iid)
                currentData = issue
                container.remove(0)
                container.add(createIssueDetailPanel(issue), BorderLayout.CENTER)
                container.revalidate()
                container.repaint()
            } catch (e: Exception) {
                Messages.showErrorDialog(container.parent, "Failed to load issue: ${e.message}", "Error")
            }
        } else if (type == DETAILS_TYPE_MILESTONE) {
            try {
                val milestone = milestoneService.getMilestone(projectId, iid)
                currentData = milestone
                container.remove(0)
                container.add(createMilestoneDetailPanel(milestone), BorderLayout.CENTER)
                container.revalidate()
                container.repaint()
            } catch (e: Exception) {
                Messages.showErrorDialog(container.parent, "Failed to load milestone: ${e.message}", "Error")
            }
        }
    }

    private fun createEmptyStatePanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val label = JBLabel("Select an item to view details")
        label.font = UIUtil.getLabelFont().deriveFont(Font.BOLD, 14f)
        panel.add(label, BorderLayout.CENTER)
        return panel
    }

    private fun createIssueDetailPanel(issue: GitLabIssue): JPanel {
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><h2>#${issue.iid}: ${issue.title}</h2></html>").apply {
                foreground = UIUtil.getLabelForeground()
            }, 0)
            .addSeparator()
            .addLabeledComponent("State:", JBLabel(issue.state.uppercase()))
            .addLabeledComponent("Author:", JBLabel(issue.author.name))
            .addLabeledComponent("Created:", JBLabel(issue.created_at.orEmpty()))
            .addLabeledComponent("Updated:", JBLabel(issue.updated_at.orEmpty()))

        issue.closed_at?.let { formPanel.addLabeledComponent("Closed:", JBLabel(it)) }

        issue.milestone?.let {
            formPanel.addLabeledComponent("Milestone:", JBLabel("${it.title} (#${it.iid})"))
        }

        if (!issue.labels.isNullOrEmpty()) {
            formPanel.addLabeledComponent("Labels:", JBLabel(issue.labels.joinToString(", ")))
        }

        issue.description?.let {
            formPanel.addSeparator()
            formPanel.addComponent(JBLabel("<html><h3>Description</h3></html>"))
            formPanel.addComponent(JTextArea(issue.description).apply {
                isEditable = false
                border = EmptyBorder(5, 5, 5, 5)
            })
        }

        issue.milestone?.let {
            formPanel.addSeparator()
            formPanel.addLabeledComponent("Assign to Milestone:", JBLabel(it.title))
        }

        val editPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val editButton = JButton("Edit Issue")
        editButton.addActionListener {
            showEditIssueDialog(issue)
        }
        editPanel.add(editButton)
        formPanel.addComponent(editPanel, 0)

        return formPanel.panel
    }

    private fun showEditIssueDialog(issue: GitLabIssue) {
        val parent = SwingUtilities.getWindowAncestor(this)
        val dialog = JDialog(parent, "Edit Issue", Dialog.ModalityType.APPLICATION_MODAL)
        dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        dialog.layout = BorderLayout()

        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(10, 10, 10, 10)
        val form = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        val titleField = JBTextField(issue.title)
        val descriptionArea = JBTextArea(issue.description.orEmpty(), 6, 40)
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = Insets(2, 2, 2, 2)
        form.add(JLabel("Title:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        form.add(titleField, gbc)

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.HORIZONTAL
        form.add(JLabel("Description:"), gbc)
        gbc.gridx = 1
        form.add(JScrollPane(descriptionArea), gbc)

        panel.add(form, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val saveButton = JButton("Save")
        val cancelButton = JButton("Cancel")

        saveButton.addActionListener {
            try {
                val updates = issueService.updateIssue(
                    projectId = currentProjectId,
                    issueIid = issue.iid,
                    title = titleField.text,
                    description = descriptionArea.text
                )
                currentData = updates
                dialog.dispose()
                setDetails(currentType, currentProjectId, currentIid)
                Messages.showInfoMessage("Issue updated successfully.", "Success")
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to update issue: ${e.message}", "Error")
            }
        }

        cancelButton.addActionListener { dialog.dispose() }

        buttonPanel.add(saveButton)
        buttonPanel.add(cancelButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        dialog.add(panel)
        dialog.pack()
        dialog.setLocationRelativeTo(this)
        dialog.isVisible = true
    }

    private fun createMilestoneDetailPanel(milestone: GitLabMilestone): JPanel {
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><h2>${milestone.title}</h2></html>").apply {
                foreground = UIUtil.getLabelForeground()
            }, 0)
            .addSeparator()
            .addLabeledComponent("State:", JBLabel(milestone.state.uppercase()))
            .addLabeledComponent("Created:", JBLabel(milestone.created_at.orEmpty()))
            .addLabeledComponent("Updated:", JBLabel(milestone.updated_at.orEmpty()))

        milestone.start_date?.let { formPanel.addLabeledComponent("Start Date:", JBLabel(it)) }
        milestone.due_date?.let { formPanel.addLabeledComponent("Due Date:", JBLabel(it)) }
        milestone.state_count.let {
            formPanel.addLabeledComponent("Progress:", JBLabel("${it} open").apply {
                font = UIUtil.getLabelFont().deriveFont(Font.BOLD, 11f)
            })
        }

        milestone.description?.let {
            formPanel.addSeparator()
            formPanel.addComponent(JBLabel("<html><h3>Description</h3></html>"))
            formPanel.addComponent(JTextArea(milestone.description).apply {
                isEditable = false
                border = EmptyBorder(5, 5, 5, 5)
            })
        }

        val editPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val editButton = JButton("Edit Milestone")
        editButton.addActionListener {
            showEditMilestoneDialog(milestone)
        }
        editPanel.add(editButton)
        formPanel.addComponent(editPanel, 0)

        return formPanel.panel
    }

    private fun showEditMilestoneDialog(milestone: GitLabMilestone) {
        val parent = SwingUtilities.getWindowAncestor(this)
        val dialog = JDialog(parent, "Edit Milestone", Dialog.ModalityType.APPLICATION_MODAL)
        dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
        dialog.layout = BorderLayout()

        val panel = JPanel(BorderLayout())
        panel.border = EmptyBorder(10, 10, 10, 10)
        val form = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        val titleField = JBTextField(milestone.title)
        val descriptionArea = JBTextArea(milestone.description.orEmpty(), 6, 40)
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true

        val startField = JBTextField(milestone.start_date.orEmpty())
        val dueField = JBTextField(milestone.due_date.orEmpty())

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = Insets(2, 2, 2, 2)
        form.add(JLabel("Title:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        form.add(titleField, gbc)

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.HORIZONTAL
        form.add(JLabel("Description:"), gbc)
        gbc.gridx = 1
        form.add(JScrollPane(descriptionArea), gbc)

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        form.add(JLabel("Start Date (YYYY-MM-DD):"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = Insets(2, 2, 2, 2)
        form.add(startField, gbc)

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        form.add(JLabel("Due Date (YYYY-MM-DD):"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = Insets(2, 2, 2, 2)
        form.add(dueField, gbc)

        panel.add(form, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val saveButton = JButton("Save")
        val cancelButton = JButton("Cancel")

        saveButton.addActionListener {
            try {
                val updates = milestoneService.updateMilestone(
                    projectId = currentProjectId,
                    milestoneIid = currentIid,
                    title = titleField.text,
                    description = descriptionArea.text.takeIf { it.isNotBlank() },
                    startDate = startField.text.takeIf { it.isNotBlank() },
                    dueDate = dueField.text.takeIf { it.isNotBlank() }
                )
                currentData = updates
                dialog.dispose()
                setDetails(currentType, currentProjectId, currentIid)
                Messages.showInfoMessage("Milestone updated successfully.", "Success")
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to update milestone: ${e.message}", "Error")
            }
        }

        cancelButton.addActionListener { dialog.dispose() }

        buttonPanel.add(saveButton)
        buttonPanel.add(cancelButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        dialog.add(panel)
        dialog.pack()
        dialog.setLocationRelativeTo(this)
        dialog.isVisible = true
    }
}
