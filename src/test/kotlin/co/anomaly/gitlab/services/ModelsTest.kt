package co.anomaly.gitlab.services

import co.anomaly.gitlab.models.GitLabIssue
import co.anomaly.gitlab.models.GitLabMilestone
import co.anomaly.gitlab.models.GitLabUser
import org.junit.Test
import org.junit.Assert.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class ModelsTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testIssueDeserialization() {
        val json = """
        {
          "id": 111,
          "iid": 1,
          "project_id": 12345,
          "title": "Test Issue",
          "description": "This is a test issue",
          "state": "opened",
          "labels": ["bug", "priority"],
          "created_at": "2024-01-15T10:30:00.000Z",
          "updated_at": "2024-01-15T11:00:00.000Z",
          "author": {
            "id": 1,
            "username": "testuser",
            "name": "Test User",
            "state": "active",
            "avatar_url": "https://gitlab.com/uploads/user/avatar/1",
            "web_url": "https://gitlab.com/testuser"
          }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)

        assertEquals(111, issue.id)
        assertEquals(1, issue.iid)
        assertEquals(12345, issue.project_id)
        assertEquals("Test Issue", issue.title)
        assertEquals("This is a test issue", issue.description)
        assertEquals("opened", issue.state)
        assertNotNull(issue.labels)
        assertEquals(2, issue.labels!!.size)
        assertEquals("bug", issue.labels[0])
        assertEquals("priority", issue.labels[1])
        assertEquals("Test User", issue.author.name)
    }

    @Test
    fun testMilestoneDeserialization() {
        val json = """
        {
          "id": 222,
          "iid": 2,
          "project_id": 12345,
          "title": "Q1 Release",
          "description": "Q1 2024 release milestone",
          "state": "active",
          "state_count": 5.0,
          "start_date": "2024-01-01",
          "due_date": "2024-03-31",
          "created_at": "2024-01-01T00:00:00.000Z",
          "updated_at": "2024-01-15T10:00:00.000Z"
        }
        """.trimIndent()

        val milestone = objectMapper.readValue(json, GitLabMilestone::class.java)

        assertEquals(222, milestone.id)
        assertEquals(2, milestone.iid)
        assertEquals(12345, milestone.project_id)
        assertEquals("Q1 Release", milestone.title)
        assertEquals("Q1 2024 release milestone", milestone.description)
        assertEquals("active", milestone.state)
        assertEquals(5.0, milestone.state_count, 0.001)
        assertEquals("2024-01-01", milestone.start_date)
        assertEquals("2024-03-31", milestone.due_date)
    }

    @Test
    fun testIssueWithMilestoneDeserialization() {
        val json = """
        {
          "id": 333,
          "iid": 3,
          "project_id": 12345,
          "title": "Issue with Milestone",
          "description": "Description",
          "state": "closed",
          "created_at": "2024-01-10T10:00:00.000Z",
          "updated_at": "2024-02-01T15:00:00.000Z",
          "closed_at": "2024-02-01T15:00:00.000Z",
          "assignees": [],
          "author": {
            "id": 1,
            "username": "developer",
            "name": "Developer",
            "state": "active",
            "avatar_url": "",
            "web_url": ""
          },
          "milestone": {
            "id": 222,
            "iid": 2,
            "project_id": 12345,
            "title": "Q1 Release",
            "description": "Q1 2024 release milestone",
            "state": "active",
            "state_count": 5.0,
            "created_at": "2024-01-01T00:00:00.000Z",
            "updated_at": "2024-01-15T10:00:00.000Z"
          }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)

        assertEquals(333, issue.id)
        assertEquals(3, issue.iid)
        assertEquals("Issue with Milestone", issue.title)
        assertEquals("closed", issue.state)
        assertNotNull(issue.milestone)
        assertEquals(222, issue.milestone!!.id)
        assertEquals("Q1 Release", issue.milestone!!.title)
    }

    @Test
    fun testIssueDeserializationWithLabelsData() {
        val json = """
        {
          "id": 444,
          "iid": 4,
          "project_id": 12345,
          "title": "Styled Issue",
          "description": "",
          "state": "opened",
          "labels": ["bug"],
          "labels_data": [
            {
              "id": 101,
              "name": "bug",
              "color": "#FF0000",
              "description": "Bug label"
            }
          ],
          "created_at": "2024-01-20T10:00:00.000Z",
          "updated_at": "2024-01-20T11:00:00.000Z",
          "author": {
            "id": 1,
            "username": "tester",
            "name": "Tester",
            "state": "active",
            "avatar_url": "",
            "web_url": ""
          }
        }
        """.trimIndent()

        val issue = objectMapper.readValue(json, GitLabIssue::class.java)

        assertEquals(444, issue.id)
        assertEquals(4, issue.iid)
        assertEquals("Styled Issue", issue.title)
        assertNotNull(issue.labels_data)
        assertEquals(1, issue.labels_data!!.size)
        assertEquals(101, issue.labels_data!![0].id)
        assertEquals("#FF0000", issue.labels_data!![0].color)
    }
}
