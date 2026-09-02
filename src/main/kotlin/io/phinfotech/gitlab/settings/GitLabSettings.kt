package io.phinfotech.gitlab.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
@State(
    name = "GitLabSettings",
    storages = [Storage("gitlab_settings.xml")]
)
class GitLabSettings : PersistentStateComponent<GitLabSettings> {

    var serverUrl: String = "https://gitlab.com"
    var privateToken: String? = null
    var autoRefresh: Boolean = true

    // Filter preferences
    var showOpenOnly: Boolean = true
    var showOpenMilestonesOnly: Boolean = true

    // State management
    var lastSelectedProjectId: Int? = null
    var lastSelectedMilestoneIid: Int? = null

    // Project filtering
    var projectFilterQuery: String = ""
    var milestoneFilterQuery: String = ""
    var issueFilterQuery: String = ""

    override fun getState(): GitLabSettings = this

    override fun loadState(state: GitLabSettings) {
        serverUrl = state.serverUrl
        privateToken = state.privateToken
        autoRefresh = state.autoRefresh
        showOpenOnly = state.showOpenOnly
        showOpenMilestonesOnly = state.showOpenMilestonesOnly
        lastSelectedProjectId = state.lastSelectedProjectId
        lastSelectedMilestoneIid = state.lastSelectedMilestoneIid
        projectFilterQuery = state.projectFilterQuery
        milestoneFilterQuery = state.milestoneFilterQuery
        issueFilterQuery = state.issueFilterQuery
    }

    companion object {
        fun getInstance(): GitLabSettings = ApplicationManager.getApplication().getService(GitLabSettings::class.java)
    }
}
