package co.anomaly.gitlab.ui

import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.services.GitLabIssueService
import co.anomaly.gitlab.services.GitLabMilestoneService
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import javax.swing.*
import java.awt.*
import java.util.concurrent.CopyOnWriteArrayList

class CreateIssueDialog(
    private val projects: List<GitLabProject>,
    private val milestones: List<GitLabMilestone>,
    private val issueService: GitLabIssueService,
    private val projectSelected: (GitLabProject) -> Unit
) : JDialog() {

    private val projectComboBox: JComboBox<String> = JComboBox<String>()
    private val milestoneComboBox: JComboBox<String?> = JComboBox<String?>()
    private val titleField: JBTextField = JBTextField()
    private val descriptionArea: JBTextArea = JBTextArea(6, 40)
    private var selectedProject: GitLabProject? = null
    private var selectedMilestoneIid: Int? = null

    init {
        title = "Create Issue"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        layout = BorderLayout()
        isModal = true

        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        // Project selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = Insets(5, 0, 5, 5)
        formPanel.add(JLabel("Project:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        projects.forEach { project ->
            projectComboBox.addItem(project.name_with_namespace)
        }
        formPanel.add(projectComboBox, gbc)

        projectComboBox.addActionListener {
            val selectedIndex = projectComboBox.selectedIndex
            if (selectedIndex >= 0) {
                selectedProject = projects[selectedIndex]
                projectSelected(selectedProject!!)
            }
        }

        // Milestone selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        formPanel.add(JLabel("Milestone:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        milestoneComboBox.addItem(null as String?)
        milestones.filter { it.state == "active" }.forEach { milestone ->
            milestoneComboBox.addItem(milestone.title)
        }
        formPanel.add(milestoneComboBox, gbc)

        milestoneComboBox.addActionListener {
            val selectedIndex = milestoneComboBox.selectedIndex
            if (selectedIndex > 0) {
                val activeMilestones = milestones.filter { it.state == "active" }
                selectedMilestoneIid = activeMilestones.getOrNull(selectedIndex - 1)?.iid
            } else {
                selectedMilestoneIid = null
            }
        }

        // Title
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        formPanel.add(JLabel("Title:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(titleField, gbc)

        // Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.BOTH
        formPanel.add(JLabel("Description:"), gbc)
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        formPanel.add(JScrollPane(descriptionArea), gbc)

        panel.add(formPanel, BorderLayout.CENTER)

        // Buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val createButton = JButton("Create")
        val cancelButton = JButton("Cancel")

        createButton.addActionListener {
            if (selectedProject == null) {
                Messages.showErrorDialog("Please select a project.", "Error")
                return@addActionListener
            }

            val title = titleField.text.trim()
            if (title.isEmpty()) {
                Messages.showErrorDialog("Title is required.", "Error")
                return@addActionListener
            }

            try {
                val issue = issueService.createIssue(
                    projectId = selectedProject!!.id,
                    title = title,
                    description = descriptionArea.text.takeIf { it.isNotBlank() },
                    milestoneIid = selectedMilestoneIid
                )

                dispose()
                Messages.showInfoMessage("Issue #${issue.iid} created successfully.", "Success")
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to create issue: ${e.message}", "Error")
            }
        }

        cancelButton.addActionListener { dispose() }

        buttonPanel.add(createButton)
        buttonPanel.add(cancelButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        add(panel)
        pack()
        setLocationRelativeTo(null)
    }
}
