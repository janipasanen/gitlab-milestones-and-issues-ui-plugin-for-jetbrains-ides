package io.phinfotech.gitlab.search

import io.phinfotech.gitlab.models.GitLabIssue
import io.phinfotech.gitlab.models.GitLabMilestone
import io.phinfotech.gitlab.models.GitLabProject
import io.phinfotech.gitlab.models.GitLabUser
import io.phinfotech.gitlab.search.SearchService
import io.phinfotech.gitlab.search.SearchResultItem
import org.junit.Test
import org.junit.Assert.*

class SearchServiceTest {

    @Test
    fun testSearchProjects() {
        val projects = listOf(
            createProject(1, "my-project", "my-project"),
            createProject(2, "another-project", "another-project")
        )
        val searchService = SearchService(projects, emptyList(), emptyList())
        
        val results = searchService.searchProjects("my")
        assertEquals(1, results.size)
        assertTrue(results[0] is SearchResultItem.ProjectResult)
    }

    @Test
    fun testSearchProjectsCaseInsensitive() {
        val projects = listOf(
            createProject(1, "MyProject", "myproject")
        )
        val searchService = SearchService(projects, emptyList(), emptyList())
        
        val results = searchService.searchProjects("my")
        assertEquals(1, results.size)
    }

    @Test
    fun testSearchProjectsNoMatch() {
        val projects = listOf(
            createProject(1, "project-one", "project-one")
        )
        val searchService = SearchService(projects, emptyList(), emptyList())
        
        val results = searchService.searchProjects("nonexistent")
        assertEquals(0, results.size)
    }

    @Test
    fun testSearchMilestones() {
        val milestones = listOf(
            createMilestone(1, 1, 1, "Release 1.0")
        )
        val searchService = SearchService(emptyList(), milestones, emptyList())
        
        val results = searchService.searchMilestones("Release")
        assertEquals(1, results.size)
        assertTrue(results[0] is SearchResultItem.MilestoneResult)
    }

    @Test
    fun testSearchIssues() {
        val issues = listOf(
            createIssue(1, 1, 1, "Fix bug in login")
        )
        val searchService = SearchService(emptyList(), emptyList(), issues)
        
        val results = searchService.searchIssues("bug")
        assertEquals(1, results.size)
        assertTrue(results[0] is SearchResultItem.IssueResult)
    }

    @Test
    fun testSearchAllProjects() {
        val projects = listOf(createProject(1, "test-project", "test-project"))
        val searchService = SearchService(projects, emptyList(), emptyList())
        
        val results = searchService.searchAll("test")
        assertTrue(results.isNotEmpty())
        assertTrue(results[0] is SearchResultItem.ProjectResult)
    }

    @Test
    fun testSearchAllReturnsEmptyForEmptyQuery() {
        val searchService = SearchService(emptyList(), emptyList(), emptyList())
        
        val results = searchService.searchAll("")
        assertEquals(0, results.size)
        
        val resultsWithSpaces = searchService.searchAll("   ")
        assertEquals(0, resultsWithSpaces.size)
    }

    @Test
    fun testSearchAllMixedResults() {
        val projects = listOf(createProject(1, "my-project", "my-project"))
        val milestones = listOf(createMilestone(1, 1, 1, "My Milestone"))
        val issues = listOf(createIssue(1, 1, 1, "My Issue"))
        val searchService = SearchService(projects, milestones, issues)
        
        val results = searchService.searchAll("my")
        assertEquals(3, results.size)
    }

    private fun createProject(id: Int, name: String, path: String): GitLabProject {
        return GitLabProject(
            id = id,
            name = name,
            name_with_namespace = name,
            path = path,
            path_with_namespace = path,
            description = null,
            web_url = "https://gitlab.com/$path",
            avatar_url = null,
            visibility = "private",
            owner = null,
            namespace = null,
            created_at = null,
            last_activity_at = null,
            star_count = null,
            topics = null,
            permissions = null
        )
    }

    private fun createMilestone(id: Int, iid: Int, projectId: Int, title: String): GitLabMilestone {
        return GitLabMilestone(
            id = id,
            iid = iid,
            project_id = projectId,
            title = title,
            description = null,
            description_html = null,
            state = "active",
            state_count = 0.0,
            total_time_spent = null,
            hours_spent = null,
            start_date = null,
            due_date = null,
            created_at = null,
            updated_at = null,
            expired = null,
            urls = null
        )
    }

    private fun createIssue(id: Int, iid: Int, projectId: Int, title: String): GitLabIssue {
        return GitLabIssue(
            id = id,
            iid = iid,
            project_id = projectId,
            title = title,
            description = null,
            description_html = null,
            state = "opened",
            state_change_performed_by = null,
            labels = null,
            labels_data = null,
            _links = null,
            merged_by = null,
            merged_by_data = null,
            created_at = null,
            updated_at = null,
            closed_at = null,
            closed_by = null,
            closed_by_data = null,
            milestone = null,
            milestone_data = null,
            assignees = null,
            assignees_data = null,
            author = GitLabUser(
                id = 1,
                username = "test",
                name = "Test User",
                state = "active",
                avatar_url = "",
                web_url = ""
            ),
            user_notes_count = 0,
            merge_requests_count = 0,
            severity = null,
            category = null,
            time_estimate = null,
            total_time_spent = null,
            human_time_estimate = null,
            human_total_time_spent = null,
            weight = null,
            has_labels = null,
            board_list = null,
            conflict_conflict = null,
            subscribe = null,
            user_stati = null,
            user_can_update = null,
            user_can_close = null,
            resource_label_events_url = null,
            resource_state_events_url = null,
            resource_milestone_events_url = null,
            resource_label_events_path = null,
            resource_milestone_events_path = null,
            confidential = null,
            task_completion_count = null,
            tasks = null,
            discussion_locked = null,
            ids = null,
            weight_calc_override = null,
            is_visible_in_dashboard = null,
            is_work_in_progress = null,
            details = null,
            iteration = null,
            version_alignments = null,
            resource_counterpart_events_path = null,
            resource_group_change_events_path = null,
            resource_mention_events_path = null,
            resource_iteration_events_path = null,
            resource_mention_events_url = null,
            web_url = null
        )
    }
}
