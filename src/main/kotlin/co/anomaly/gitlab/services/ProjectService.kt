package co.anomaly.gitlab.services

import co.anomaly.gitlab.api.GitLabApiClient
import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabProject

class GitLabProjectService(private val client: GitLabApiClient) {

    fun getProjects(page: Int, perPage: Int): List<GitLabProject> {
        return client.getPage(
            "/projects",
            page,
            perPage,
            GitLabProject::class.java
        )
    }

    fun searchProjects(query: String): List<GitLabProject> {
        val projects = client.get(
            "/projects",
            mapOf("search" to query, "per_page" to "100"),
            List::class.java
        )
        return projects.filterIsInstance<GitLabProject>()
    }

    fun getProject(id: Int): GitLabProject {
        return client.get("/projects/$id", GitLabProject::class.java)
    }

    fun getProjectByFullPath(fullPath: String): GitLabProject {
        return client.get("/projects/${java.net.URLEncoder.encode(fullPath, "UTF-8")}", GitLabProject::class.java)
    }
}
