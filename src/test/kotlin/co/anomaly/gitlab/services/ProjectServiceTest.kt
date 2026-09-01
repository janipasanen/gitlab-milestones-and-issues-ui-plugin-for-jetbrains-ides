package co.anomaly.gitlab.services

import co.anomaly.gitlab.models.GitLabProject
import org.junit.Test
import org.junit.Assert.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class ProjectServiceTest {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testProjectDeserialization() {
        val json = """
        {
          "id": 12345,
          "name": "Test Project",
          "name_with_namespace": "User / Test Project",
          "path": "test-project",
          "path_with_namespace": "user/test-project",
          "description": "A test project",
          "web_url": "https://gitlab.com/user/test-project",
          "avatar_url": null,
          "visibility": "public"
        }
        """.trimIndent()

        val project = objectMapper.readValue(json, GitLabProject::class.java)

        assertEquals(12345, project.id)
        assertEquals("Test Project", project.name)
        assertEquals("User / Test Project", project.name_with_namespace)
        assertEquals("test-project", project.path)
        assertEquals("A test project", project.description)
        assertEquals("https://gitlab.com/user/test-project", project.web_url)
        assertEquals("public", project.visibility)
    }

    @Test
    fun testProjectDeserializationWithOwner() {
        val json = """
        {
          "id": 67890,
          "name": "Another Project",
          "name_with_namespace": "Group / Another Project",
          "path": "another-project",
          "path_with_namespace": "group/another-project",
          "description": "Test description",
          "web_url": "https://gitlab.com/group/another-project",
          "avatar_url": "https://gitlab.com/uploads/project/avatar/12345",
          "visibility": "private",
          "owner": {
            "id": 100,
            "username": "testuser",
            "name": "Test User",
            "state": "active",
            "avatar_url": "https://gitlab.com/uploads/user/avatar/100",
            "web_url": "https://gitlab.com/testuser"
          }
        }
        """.trimIndent()

        val project = objectMapper.readValue(json, GitLabProject::class.java)

        assertEquals(67890, project.id)
        assertEquals("Another Project", project.name)
        assertNotNull(project.owner)
        assertEquals("testuser", project.owner?.username)
        assertEquals("Test User", project.owner?.name)
    }
}
