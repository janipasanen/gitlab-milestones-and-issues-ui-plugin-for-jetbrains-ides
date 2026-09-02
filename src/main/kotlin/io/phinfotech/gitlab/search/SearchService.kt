package io.phinfotech.gitlab.search

import io.phinfotech.gitlab.models.GitLabIssue
import io.phinfotech.gitlab.models.GitLabMilestone
import io.phinfotech.gitlab.models.GitLabPipeline
import io.phinfotech.gitlab.models.GitLabProject

sealed class SearchResultItem {
    abstract val displayName: String
    abstract val type: String
    abstract val id: Int

    data class ProjectResult(val project: GitLabProject) : SearchResultItem() {
        override val displayName: String get() = project.name_with_namespace
        override val type: String get() = "Project"
        override val id: Int get() = project.id
    }

    data class MilestoneResult(val milestone: GitLabMilestone, val projectName: String) : SearchResultItem() {
        override val displayName: String get() = milestone.title
        override val type: String get() = "Milestone"
        override val id: Int get() = milestone.id
    }

    data class IssueResult(val issue: GitLabIssue, val projectName: String) : SearchResultItem() {
        override val displayName: String get() = "#${issue.iid}: ${issue.title}"
        override val type: String get() = "Issue"
        override val id: Int get() = issue.id
    }

    data class PipelineResult(val pipeline: GitLabPipeline, val projectName: String) : SearchResultItem() {
        override val displayName: String get() = "Pipeline #${pipeline.iid} - ${pipeline.status}"
        override val type: String get() = "Pipeline"
        override val id: Int get() = pipeline.id
    }
}

class SearchService(
    private val projects: List<GitLabProject>,
    private val milestones: List<GitLabMilestone>,
    private val issues: List<GitLabIssue>
) {

    fun searchProjects(query: String): List<SearchResultItem> {
        val q = query.lowercase()
        return projects
            .filter { it.name.lowercase().contains(q) || it.path.lowercase().contains(q) }
            .map { SearchResultItem.ProjectResult(it) }
    }

    fun searchMilestones(query: String): List<SearchResultItem> {
        val q = query.lowercase()
        return milestones
            .filter { it.title.lowercase().contains(q) }
            .map { SearchResultItem.MilestoneResult(it, it.toString()) }
    }

    fun searchIssues(query: String): List<SearchResultItem> {
        val q = query.lowercase()
        return issues
            .filter { it.title.lowercase().contains(q) || it.description?.lowercase()?.contains(q) == true }
            .map { SearchResultItem.IssueResult(it, it.toString()) }
    }

    fun searchAll(query: String): List<SearchResultItem> {
        if (query.isBlank()) return emptyList()

        return searchProjects(query) + searchMilestones(query) + searchIssues(query)
    }
}
