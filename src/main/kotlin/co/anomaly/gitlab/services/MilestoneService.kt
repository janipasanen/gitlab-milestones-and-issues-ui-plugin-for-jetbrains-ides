package co.anomaly.gitlab.services

import co.anomaly.gitlab.api.GitLabApiClient
import co.anomaly.gitlab.models.GitLabMilestone

class GitLabMilestoneService(private val client: GitLabApiClient) {

    fun getMilestones(projectId: Int, state: String = "all", page: Int = 1): List<GitLabMilestone> {
        return client.getPage(
            "/projects/$projectId/milestones",
            page,
            100,
            GitLabMilestone::class.java
        )
    }

    fun getMilestones(projectId: Int, state: String, title: String?): List<GitLabMilestone> {
        val params = mutableMapOf("state" to state)
        title?.let { params["title"] = it }

        val result = client.get(
            "/projects/$projectId/milestones",
            params,
            List::class.java
        )
        return result.filterIsInstance<GitLabMilestone>()
    }

    fun getMilestone(projectId: Int, milestoneIid: Int): GitLabMilestone {
        return client.get(
            "/projects/$projectId/milestones/$milestoneIid",
            GitLabMilestone::class.java
        )
    }

    fun createMilestone(
        projectId: Int,
        title: String,
        description: String? = null,
        startDateFormat: String? = null,
        dueDateFormat: String? = null
    ): GitLabMilestone {
        val body = CreateMilestoneRequest(title, description, startDateFormat, dueDateFormat)
        return client.post("/projects/$projectId/milestones", body, GitLabMilestone::class.java)
    }

    fun updateMilestone(
        projectId: Int,
        milestoneIid: Int,
        title: String? = null,
        description: String? = null,
        startDate: String? = null,
        dueDate: String? = null,
        stateEvent: String? = null
    ): GitLabMilestone {
        val body = UpdateMilestoneRequest(title, description, startDate, dueDate, stateEvent)
        return client.put("/projects/$projectId/milestones/$milestoneIid", body, GitLabMilestone::class.java)
    }

    fun deleteMilestone(projectId: Int, milestoneIid: Int) {
        client.delete<Unit>("/projects/$projectId/milestones/$milestoneIid")
    }

    data class CreateMilestoneRequest(
        val title: String,
        val description: String? = null,
        val start_date: String? = null,
        val due_date: String? = null
    )

    data class UpdateMilestoneRequest(
        val title: String? = null,
        val description: String? = null,
        val start_date: String? = null,
        val due_date: String? = null,
        val state_event: String? = null
    )
}
