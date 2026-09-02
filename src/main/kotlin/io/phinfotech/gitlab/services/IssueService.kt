package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.models.GitLabIssue

class GitLabIssueService(private val client: GitLabApiClient) {

    fun getIssues(projectId: Int, state: String = "all", milestoneIid: Int? = null, page: Int = 1): List<GitLabIssue> {
        val params = mutableMapOf("state" to state, "per_page" to "100")
        milestoneIid?.let { params["milestone_iid"] = it.toString() }

        val result = client.get(
            "/projects/$projectId/issues",
            params,
            List::class.java
        )
        return result.filterIsInstance<GitLabIssue>()
    }

    fun searchIssues(query: String, projectId: Int? = null): List<GitLabIssue> {
        val params = mutableMapOf("search" to query, "per_page" to "100")
        projectId?.let { params["project_id"] = it.toString() }

        val result = client.get(
            "/issues",
            params,
            List::class.java
        )
        return result.filterIsInstance<GitLabIssue>()
    }

    fun searchMilestones(query: String, projectId: Int): List<io.phinfotech.gitlab.models.GitLabMilestone> {
        val result = client.get(
            "/projects/$projectId/milestones",
            mapOf("search" to query, "per_page" to "100"),
            List::class.java
        )
        return result.filterIsInstance<io.phinfotech.gitlab.models.GitLabMilestone>()
    }

    fun getIssue(projectId: Int, issueIid: Int): GitLabIssue {
        return client.get(
            "/projects/$projectId/issues/$issueIid",
            GitLabIssue::class.java
        )
    }

    fun createIssue(
        projectId: Int,
        title: String,
        description: String? = null,
        milestoneIid: Int? = null,
        assigneeIds: List<Int>? = null,
        labels: List<String>? = null,
        confidential: Boolean? = null
    ): GitLabIssue {
        val body = CreateIssueRequest(
            title = title,
            description = description,
            milestone_id = milestoneIid,
            assignee_ids = assigneeIds,
            labels = labels,
            confidential = confidential
        )
        return client.post("/projects/$projectId/issues", body, GitLabIssue::class.java)
    }

    fun updateIssue(
        projectId: Int,
        issueIid: Int,
        title: String? = null,
        description: String? = null,
        stateEvent: String? = null,
        milestoneIid: Int? = null,
        assigneeIds: List<Int>? = null,
        labels: List<String>? = null,
        confidential: Boolean? = null
    ): GitLabIssue {
        val body = UpdateIssueRequest(
            title = title,
            description = description,
            state_event = stateEvent,
            milestone_id = milestoneIid,
            assignee_ids = assigneeIds,
            labels = labels,
            confidential = confidential
        )
        return client.put("/projects/$projectId/issues/$issueIid", body, GitLabIssue::class.java)
    }

    fun addIssueNote(projectId: Int, issueIid: Int, body: String): GitLabIssue.Note {
        val bodyReq = GitLabIssue.NoteRequest(body)
        return client.post(
            "/projects/$projectId/issues/$issueIid/notes",
            bodyReq,
            GitLabIssue.Note::class.java
        )
    }

    fun getIssueNotes(projectId: Int, issueIid: Int): List<GitLabIssue.Note> {
        val result = client.get(
            "/projects/$projectId/issues/$issueIid/notes",
            mapOf("per_page" to "100"),
            List::class.java
        )
        return result.filterIsInstance<GitLabIssue.Note>()
    }

    data class CreateIssueRequest(
        val title: String,
        val description: String? = null,
        val milestone_id: Int? = null,
        val assignee_ids: List<Int>? = null,
        val labels: List<String>? = null,
        val confidential: Boolean? = null
    )

    data class UpdateIssueRequest(
        val title: String? = null,
        val description: String? = null,
        val state_event: String? = null,
        val milestone_id: Int? = null,
        val assignee_ids: List<Int>? = null,
        val labels: List<String>? = null,
        val confidential: Boolean? = null
    )
}
