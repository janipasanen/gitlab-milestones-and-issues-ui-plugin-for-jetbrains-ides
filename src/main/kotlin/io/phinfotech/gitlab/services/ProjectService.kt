package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.models.GitLabIssue
import io.phinfotech.gitlab.models.GitLabMilestone
import io.phinfotech.gitlab.models.GitLabProject

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
