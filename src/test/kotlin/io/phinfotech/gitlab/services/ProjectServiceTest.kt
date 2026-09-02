package io.phinfotech.gitlab.services

import io.phinfotech.gitlab.models.GitLabProject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.Test
import org.junit.Assert.*

class ProjectServiceTest {

    private val objectMapper = ObjectMapper().registerModule(kotlinModule())

    @Test
    fun testProjectDeserialization() {
        val json = """
        {
            "id": 1,
            "name": "Test Project",
            "name_with_namespace": "Test User / Test Project",
            "path": "test-project",
            "path_with_namespace": "test-user/test-project",
            "description": "Test project description",
            "web_url": "https://gitlab.com/test-user/test-project",
            "avatar_url": null,
            "visibility": "private",
            "created_at": "2024-01-01T00:00:00Z",
            "last_activity_at": "2024-01-02T00:00:00Z",
            "star_count": 5,
            "namespace": {
                "id": 1,
                "name": "Test User",
                "path": "test-user",
                "kind": "user",
                "full_path": "test-user",
                "web_url": "https://gitlab.com/test-user"
            }
        }
        """.trimIndent()

        val project = objectMapper.readValue(json, GitLabProject::class.java)
        
        assertEquals(1, project.id)
        assertEquals("Test Project", project.name)
        assertEquals("Test User / Test Project", project.name_with_namespace)
        assertEquals("test-project", project.path)
        assertEquals("test-user/test-project", project.path_with_namespace)
        assertEquals("Test project description", project.description)
        assertEquals("https://gitlab.com/test-user/test-project", project.web_url)
        assertEquals(5, project.star_count)
    }

    @Test
    fun testProjectDeserializationWithOwner() {
        val json = """
        {
            "id": 1,
            "name": "Project with Owner",
            "name_with_namespace": "Group / Project with Owner",
            "path": "project-with-owner",
            "path_with_namespace": "group/project-with-owner",
            "description": null,
            "web_url": "https://gitlab.com/group/project-with-owner",
            "avatar_url": null,
            "visibility": "public",
            "owner": {
                "id": 100,
                "username": "groupowner",
                "name": "Group Owner",
                "state": "active",
                "avatar_url": "",
                "web_url": ""
            },
            "namespace": {
                "id": 10,
                "name": "Group",
                "path": "group",
                "kind": "group",
                "full_path": "group",
                "web_url": "https://gitlab.com/group"
            }
        }
        """.trimIndent()

        val project = objectMapper.readValue(json, GitLabProject::class.java)
        
        assertNotNull(project.owner)
        assertEquals("groupowner", project.owner?.username)
        assertEquals("Group Owner", project.owner?.name)
        assertEquals("active", project.owner?.state)
    }
}
