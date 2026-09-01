package co.anomaly.gitlab.search

import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabProject
import co.anomaly.gitlab.models.GitLabUser
import org.junit.Test
import org.junit.Assert.*

class SearchServiceTest {

    private fun createProject(id: Int, name: String, path: String): GitLabProject {
        return GitLabProject(
            id = id,
            name = name,
            name_with_namespace = "User / $name",
            path = path,
            path_with_namespace = "user/$path",
            description = null,
            web_url = "",
            avatar_url = null,
            visibility = "public",
            created_at = null,
            last_activity_at = null
        )
    }

    private fun createMilestone(id: Int, iid: Int, title: String, state: String): GitLabMilestone {
        return GitLabMilestone(
            id = id,
            iid = iid,
            project_id = 1,
            title = title,
            description = null,
            description_html = null,
            state = state,
            state_count = 0.0,
            total_time_spent = null,
            hours_spent = null,
            start_date = null,
            due_date = null,
            created_at = null,
            updated_at = null,
            expired = null
        )
    }

    private fun createIssue(id: Int, iid: Int, title: String, state: String): GitLabIssue {
        return GitLabIssue(
            id = id,
            iid = iid,
            project_id = 1,
            title = title,
            description = null,
            description_html = null,
            state = state,
            author = GitLabUser(
                id = 1,
                username = "test",
                name = "Test",
                state = "active",
                avatar_url = "",
                web_url = ""
            ),
            created_at = null,
            updated_at = null,
            closed_at = null,
            severity = null,
            category = null
        )
    }

    @Test
    fun testSearchProjects() {
        val projects = listOf(
            createProject(1, "Backend API", "backend-api"),
            createProject(2, "Frontend App", "frontend-app"),
            createProject(3, "Documentation", "docs")
        )

        val searchService = SearchService(projects, emptyList(), emptyList())

        val results = searchService.searchProjects("backend")
        assertEquals(1, results.size)
        assertEquals("Backend API", (results[0] as SearchResultItem.ProjectResult).project.name)
    }

    @Test
    fun testSearchProjectsCaseInsensitive() {
        val projects = listOf(
            createProject(1, "Backend API", "backend-api")
        )

        val searchService = SearchService(projects, emptyList(), emptyList())

        val results = searchService.searchProjects("BACKEND")
        assertEquals(1, results.size)
    }

    @Test
    fun testSearchProjectsNoMatch() {
        val projects = listOf(
            createProject(1, "Backend API", "backend-api"),
            createProject(2, "Frontend App", "frontend-app")
        )

        val searchService = SearchService(projects, emptyList(), emptyList())

        val results = searchService.searchProjects("nonexistent")
        assertEquals(0, results.size)
    }

    @Test
    fun testSearchMilestones() {
        val milestones = listOf(
            createMilestone(1, 1, "Q1 Release", "active"),
            createMilestone(2, 2, "Q2 Release", "active"),
            createMilestone(3, 3, "Legacy Release", "closed")
        )

        val searchService = SearchService(emptyList(), milestones, emptyList())

        val results = searchService.searchMilestones("Q1")
        assertEquals(1, results.size)
        assertEquals("Q1 Release", (results[0] as SearchResultItem.MilestoneResult).milestone.title)
    }

    @Test
    fun testSearchIssues() {
        val issues = listOf(
            createIssue(1, 1, "Fix login bug", "opened"),
            createIssue(2, 2, "Add dark mode", "opened"),
            createIssue(3, 3, "Update dependencies", "closed")
        )

        val searchService = SearchService(emptyList(), emptyList(), issues)

        val results = searchService.searchIssues("login")
        assertEquals(1, results.size)
        assertEquals("#1: Fix login bug", (results[0] as SearchResultItem.IssueResult).displayName)
    }

    @Test
    fun testSearchAllProjects() {
        val projects = listOf(
            createProject(1, "Web App", "web-app")
        )

        val searchService = SearchService(projects, emptyList(), emptyList())

        val results = searchService.searchAll("web")
        assertEquals(1, results.size)
        assertTrue(results[0] is SearchResultItem.ProjectResult)
    }

    @Test
    fun testSearchAllReturnsEmptyForEmptyQuery() {
        val searchService = SearchService(emptyList(), emptyList(), emptyList())

        val results = searchService.searchAll("")
        assertEquals(0, results.size)
    }

    @Test
    fun testSearchAllMixedResults() {
        val projects = listOf(createProject(1, "App", "app"))
        val milestones = listOf(createMilestone(1, 1, "Release", "active"))
        val issues = listOf(createIssue(1, 1, "Bug fix", "opened"))

        val searchService = SearchService(projects, milestones, issues)

        val results = searchService.searchAll("r")
        assertEquals(1, results.size) // Milestone matches
    }
}
