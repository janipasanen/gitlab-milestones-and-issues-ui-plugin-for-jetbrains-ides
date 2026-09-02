package io.phinfotech.gitlab.api

import io.phinfotech.gitlab.api.GitLabApiClient
import io.phinfotech.gitlab.models.GitLabProject
import org.junit.Test
import org.junit.Assert.*

class GitLabApiClientTest {

    @Test
    fun testApiClientCreation() {
        val client = GitLabApiClient("https://gitlab.com", "test-token")
        assertNotNull(client)
        assertTrue(client.isConfigured())
        assertTrue(client.isAuthenticated())
    }

    @Test
    fun testApiClientCreationWithoutToken() {
        val client = GitLabApiClient("https://gitlab.com", null)
        assertNotNull(client)
        assertTrue(client.isConfigured())
        assertFalse(client.isAuthenticated())
    }

    @Test
    fun testApiClientCreationWithEmptyToken() {
        val client = GitLabApiClient("https://gitlab.com", "")
        assertNotNull(client)
        assertTrue(client.isConfigured())
        assertFalse(client.isAuthenticated())
    }

    @Test
    fun testBaseUrlNormalization() {
        val clientWithTrailingSlash = GitLabApiClient("https://gitlab.com/", "token")
        val clientWithoutTrailingSlash = GitLabApiClient("https://gitlab.com", "token")
        
        assertEquals(clientWithTrailingSlash.apiEndpoint, clientWithoutTrailingSlash.apiEndpoint)
        assertEquals("https://gitlab.com/api/v4", clientWithTrailingSlash.apiEndpoint)
    }
}
