package co.anomaly.gitlab.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import javax.swing.*
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets

class GitLabSettingsConfigurable : Configurable {

    private var textFieldServerUrl: JTextField? = null
    private var textFieldPrivateToken: JPasswordField? = null
    private var checkBoxAutoRefresh: JCheckBox? = null
    private var showOpenMilestonesOnly: JCheckBox? = null

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        val form = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
        }

        textFieldServerUrl = JTextField(GitLabSettings.getInstance().serverUrl).apply { columns = 40 }
        textFieldPrivateToken = JPasswordField(GitLabSettings.getInstance().privateToken.orEmpty()).apply { columns = 40 }
        checkBoxAutoRefresh = JCheckBox("Auto-refresh on window open", GitLabSettings.getInstance().autoRefresh)
        showOpenMilestonesOnly = JCheckBox("Show open milestones only by default", GitLabSettings.getInstance().showOpenMilestonesOnly)

        addFormField(form, gbc, 0, "GitLab Server URL:", textFieldServerUrl)
        addFormField(form, gbc, 1, "Private Token:", textFieldPrivateToken)
        addFormField(form, gbc, 2, null, checkBoxAutoRefresh)
        addFormField(form, gbc, 3, null, showOpenMilestonesOnly)

        panel.add(form, BorderLayout.CENTER)
        return panel
    }

    override fun isModified(): Boolean {
        return textFieldServerUrl?.getText().orEmpty() != GitLabSettings.getInstance().serverUrl ||
            String(textFieldPrivateToken?.password ?: charArrayOf()).orEmpty() != GitLabSettings.getInstance().privateToken ||
            checkBoxAutoRefresh?.isSelected != GitLabSettings.getInstance().autoRefresh ||
            showOpenMilestonesOnly?.isSelected != GitLabSettings.getInstance().showOpenMilestonesOnly
    }

    override fun apply() {
        val settings = GitLabSettings.getInstance()
        settings.serverUrl = textFieldServerUrl?.getText().orEmpty()
        settings.privateToken = String(textFieldPrivateToken?.password ?: charArrayOf())
        settings.autoRefresh = checkBoxAutoRefresh?.isSelected ?: false
        settings.showOpenMilestonesOnly = showOpenMilestonesOnly?.isSelected ?: true
        Messages.showInfoMessage("GitLab settings saved successfully.", "Settings Saved")
    }

    override fun reset() {
        val settings = GitLabSettings.getInstance()
        textFieldServerUrl?.text = settings.serverUrl
        textFieldPrivateToken?.setText(settings.privateToken.orEmpty())
        checkBoxAutoRefresh?.isSelected = settings.autoRefresh
        showOpenMilestonesOnly?.isSelected = settings.showOpenMilestonesOnly
    }

    override fun getDisplayName(): String = "GitLab"

    private fun addFormField(parent: JPanel, gbc: GridBagConstraints, row: Int, label: String?, field: JComponent?) {
        gbc.gridx = 0
        gbc.gridy = row
        gbc.weightx = 0.0
        label?.let {
            parent.add(JLabel(it), gbc)
            gbc.gridx = 1
            gbc.weightx = 1.0
        }
        field?.let { parent.add(it, gbc) }
    }
}
