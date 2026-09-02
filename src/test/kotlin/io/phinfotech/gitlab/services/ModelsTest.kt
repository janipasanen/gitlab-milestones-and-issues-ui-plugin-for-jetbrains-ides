package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.models.GitLabIssue
import io.phinfotech.gitlab.models.GitLabMilestone
import io.phinfotech.gitlab.models.GitLabUser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.Test
import org.junit.Assert.*

class ModelsTest {

    private val objectMapper = ObjectMapper().registerModule(kotlinModule())

    @Test
    fun testIssueDeserialization() {
        val json = """
        {
            "id": 1,
            "iid": 1,
            "project_id": 1,
            "title": "Test Issue",
            "description": "Test description",
            "state": "opened",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-02T00:00:00Z",
            "author": {
                "id": 1,
                "username": "testuser",
                "name": "Test User",
                "state": "active",
                "avatar_url": "",
                "web_url": ""
            }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)
        
        assertEquals(1, issue.id)
        assertEquals(1, issue.iid)
        assertEquals("Test Issue", issue.title)
        assertEquals("Test description", issue.description)
        assertEquals("opened", issue.state)
    }

    @Test
    fun testMilestoneDeserialization() {
        val json = """
        {
            "id": 1,
            "iid": 1,
            "project_id": 1,
            "title": "Test Milestone",
            "description": "Test milestone description",
            "state": "active",
            "state_count": 5.0,
            "start_date": "2024-01-01",
            "due_date": "2024-12-31",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-02T00:00:00Z"
        }
        """.trimIndent()

        val milestone = objectMapper.readValue(json, GitLabMilestone::class.java)
        
        assertEquals(1, milestone.id)
        assertEquals(1, milestone.iid)
        assertEquals("Test Milestone", milestone.title)
        assertEquals("active", milestone.state)
        assertEquals(5.0, milestone.state_count, 0.01)
    }

    @Test
    fun testIssueWithMilestoneDeserialization() {
        val json = """
        {
            "id": 1,
            "iid": 1,
            "project_id": 1,
            "title": "Issue with Milestone",
            "state": "opened",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-02T00:00:00Z",
            "milestone": {
                "id": 10,
                "iid": 1,
                "project_id": 1,
                "title": "My Milestone",
                "description": "Milestone desc",
                "description_html": "",
                "state": "active",
                "state_count": 2.0,
                "total_time_spent": null,
                "hours_spent": null,
                "start_date": "2024-01-01",
                "due_date": "2024-06-30",
                "created_at": "2024-01-01T00:00:00Z",
                "updated_at": "2024-01-02T00:00:00Z",
                "expired": false
            },
            "author": {
                "id": 1,
                "username": "test",
                "name": "Test User",
                "state": "active",
                "avatar_url": "",
                "web_url": ""
            }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)
        
        assertNotNull(issue.milestone)
        assertEquals("My Milestone", issue.milestone?.title)
        assertEquals(1, issue.milestone?.iid)
    }

    @Test
    fun testIssueDeserializationWithLabelsData() {
        val json = """
        {
            "id": 1,
            "iid": 1,
            "project_id": 1,
            "title": "Issue with Labels",
            "state": "opened",
            "created_at": "2024-01-01T00:00:00Z",
            "updated_at": "2024-01-02T00:00:00Z",
            "labels": ["bug", "critical"],
            "labels_data": [
                {
                    "id": 1,
                    "name": "bug",
                    "color": "#ff0000",
                    "text_color": "#ffffff"
                },
                {
                    "id": 2,
                    "name": "critical",
                    "color": "#ff0000",
                    "text_color": "#ffffff"
                }
            ],
            "author": {
                "id": 1,
                "username": "test",
                "name": "Test User",
                "state": "active",
                "avatar_url": "",
                "web_url": ""
            }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)
        
        assertNotNull(issue.labels)
        assertEquals(2, issue.labels?.size)
        assertEquals("bug", issue.labels!![0])
        assertEquals("critical", issue.labels[1])
        
        assertNotNull(issue.labels_data)
        assertEquals(2, issue.labels_data?.size)
    }
}
