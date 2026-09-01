package co.anomaly.gitlab.api

import co.anomaly.gitlab.models.GitLabProject
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class GitLabApiClientTest {

    @Test
    fun testApiClientCreation() {
        val client = GitLabApiClient("https://gitlab.com", "test-token")
        assertEquals("https://gitlab.com/api/v4", client.apiEndpoint)
        assertTrue(client.isConfigured())
        assertTrue(client.isAuthenticated())
    }

    @Test
    fun testApiClientCreationWithoutToken() {
        val client = GitLabApiClient("https://gitlab.com", null)
        assertEquals("https://gitlab.com/api/v4", client.apiEndpoint)
        assertTrue(client.isConfigured())
        assertFalse(client.isAuthenticated())
    }

    @Test
    fun testApiClientCreationWithEmptyToken() {
        val client = GitLabApiClient("https://gitlab.com", "")
        assertEquals("https://gitlab.com/api/v4", client.apiEndpoint)
        assertTrue(client.isConfigured())
        assertFalse(client.isAuthenticated())
    }

    @Test
    fun testBaseUrlNormalization() {
        val client1 = GitLabApiClient("https://gitlab.com/", "token")
        val client2 = GitLabApiClient("https://gitlab.com", "token")
        assertEquals(client1.apiEndpoint, client2.apiEndpoint)
    }
}
