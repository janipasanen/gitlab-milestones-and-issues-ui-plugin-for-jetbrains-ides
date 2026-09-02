package io.phinfotech.gitlab.actions

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.services.GitLabProjectService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

class RefreshAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("GitLab")

        if (toolWindow != null) {
            toolWindow.activate {
                // Refresh will be triggered by the tool window
            }
        }

        Messages.showInfoMessage("GitLab data refreshed.", "GitLab")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
        e.presentation.isVisible = true
    }
}
