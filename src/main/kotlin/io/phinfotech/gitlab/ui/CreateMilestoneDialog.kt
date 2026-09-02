package io.phinfotech.gitlab.ui

import io.phinfotech.gitlab.services.GitLabMilestoneService
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import javax.swing.*
import java.awt.*

class CreateMilestoneDialog(
    private val projectId: Int,
    private val milestoneService: GitLabMilestoneService,
    private val projectSelected: (Int) -> Unit
) : JDialog() {

    private val titleField: JBTextField = JBTextField()
    private val descriptionArea: JBTextArea = JBTextArea(6, 40)
    private val startDateField: JBTextField = JBTextField()
    private val dueDateField: JBTextField = JBTextField()

    init {
        title = "Create Milestone"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        layout = BorderLayout()
        isModal = true

        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        val formPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.insets = Insets(5, 0, 5, 5)
        formPanel.add(JLabel("Title:"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(titleField, gbc)

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.BOTH
        formPanel.add(JLabel("Description:"), gbc)
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        formPanel.add(JScrollPane(descriptionArea), gbc)

        // Start date
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        formPanel.add(JLabel("Start Date (YYYY-MM-DD):"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(startDateField, gbc)

        // Due date
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
        formPanel.add(JLabel("Due Date (YYYY-MM-DD):"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        formPanel.add(dueDateField, gbc)

        panel.add(formPanel, BorderLayout.CENTER)

        // Buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val createButton = JButton("Create")
        val cancelButton = JButton("Cancel")

        createButton.addActionListener {
            val title = titleField.text.trim()
            if (title.isEmpty()) {
                Messages.showErrorDialog("Title is required.", "Error")
                return@addActionListener
            }

            try {
                val milestone = milestoneService.createMilestone(
                    projectId = projectId,
                    title = title,
                    description = descriptionArea.text.takeIf { it.isNotBlank() },
                    startDateFormat = startDateField.text.takeIf { it.isNotBlank() },
                    dueDateFormat = dueDateField.text.takeIf { it.isNotBlank() }
                )

                dispose()
                Messages.showInfoMessage("Milestone '$title' created successfully.", "Success")
            } catch (e: Exception) {
                Messages.showErrorDialog("Failed to create milestone: ${e.message}", "Error")
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
